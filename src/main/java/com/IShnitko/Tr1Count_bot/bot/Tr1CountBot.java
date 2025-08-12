package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.util.exception.UserAlreadyInGroupException;
import com.IShnitko.Tr1Count_bot.util.exception.UserNotFoundException;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Tr1CountBot extends TelegramLongPollingBot {
    private static final Logger LOG = LoggerFactory.getLogger(Tr1CountBot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String JOIN = "/join";
    private static final String CREATE = "/create";
    private static final String ROOMS = "/rooms";
    private static final String BALANCE = "/balance";
    private static final String ADD_EXPENSE = "/add_expense";
    private static final String MEMBERS = "/members";
    private static final String BACK_BUTTON = "Return";
    private static final String BACK_COMMAND = "/back";

    private final GroupService groupService;
    private final BalanceService balanceService;
    private final UserStateManager userStateManager;

    @Autowired
    public Tr1CountBot(@Value("${bot.token}") String botToken, GroupService groupService, BalanceService balanceService, UserStateManager userStateManager) {
        super(botToken);
        this.groupService = groupService;
        this.balanceService = balanceService;
        this.userStateManager = userStateManager;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        org.telegram.telegrambots.meta.api.objects.User telegramUser = update.getMessage().getFrom();
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        Long userId = telegramUser.getId();

        // register or find user
        userService.findOrCreateUser(telegramUser);

        // getting state
        UserState userState = userStateManager.getState(chatId);

        LOG.info("Processing message from chatId: {} with user state: {}", chatId, userState);

        switch (userState) {
            case DEFAULT -> handleDefaultState(chatId, userId, messageText);
            case IN_THE_GROUP -> handleInGroupState(chatId, userId, messageText);
            default -> unknownCommand(chatId);
        }
    }

    private void handleInGroupState(Long chatId, Long userId, String messageText) {
        String groupCode = userStateManager.getChosenGroup(chatId);

        // displayGroup(chatId, groupCode);

        switch (messageText) {
            case HELP -> groupHelpCommand(chatId);
            case BALANCE -> balanceCommand(chatId, groupCode);
            default -> unknownCommand(chatId);
        }
    }

    private void handleDefaultState(Long chatId, Long userId, String messageText) {

        String command = messageText.split(" ")[0];

        switch (command) {
            case START -> {
                if (messageText.startsWith(START + " invite_")) {
                    handleInvitation(chatId, userId, messageText);
                } else {
                    startCommand(chatId);
                }
            }
            case HELP -> helpCommand(chatId);
            case JOIN -> {
                if (messageText.length() > JOIN.length()) {
                    handleJoinGroup(chatId, userId, messageText);
                } else {
                    joinCommand(chatId, userId);
                }
            }
            case BACK_COMMAND -> handleBackCommand(chatId);
            case CREATE -> {
                if (messageText.length() > CREATE.length()) {
                    handleCreateGroup(chatId, userId, messageText);
                } else {
                    createCommand(chatId);
                }
            }
            case GROUPS -> chooseGroup(chatId, userId);
            default -> unknownCommand(chatId);
        }
    }


    private void chooseGroup(Long chatId, Long userId) {
        List<Group> groups = groupService.getGroupsForUser(userId);
        if (groups.isEmpty()) {
            sendMessage(chatId, "You are not a part of any group yet!");
        } else {
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row;
            for (var group : groups) {
                row = new ArrayList<>();
                row.add(InlineKeyboardButton.builder()
                                .text(group.getName())
                                .callbackData(group.getId())
                        .build());
                rows.add(row);
            }
            inlineKeyboard.setKeyboard(rows);
            var text = "Choose group:";

            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .replyMarkup(inlineKeyboard)
                    .build();
            try {
                execute(message);
            } catch (TelegramApiException e) {
                LOG.error("Error while getting from currency", e);
            }
        }
    }

    private void balanceCommand(Long chatId, String groupCode) {
        Map<User, BigDecimal> balance = balanceService.calculateBalance(groupCode);
        StringBuilder balanceText = new StringBuilder();
        balanceText.append("Balance for your group is:\n\n");

        for (Map.Entry<User, BigDecimal> entry : balance.entrySet()) {
            User user = entry.getKey();
            BigDecimal userBalance = entry.getValue();

            balanceText.append(String.format("• %s: %s\n", user.getName(), userBalance));
        }

        String result = balanceText.toString();
        sendMessage(chatId, result);
    }

    private void groupHelpCommand(Long chatId) { // TODO: create text for group help command
        var text = """
                Group commands:
                
                This is a TriCount bot! More description coming soon!
                for now use commands:
                /balance
                /add_expense
                /members
                /help
                """;
        sendMessage(chatId, text);
    }

    private void displayGroup(Long chatId, String groupCode) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                        .text("Balance of the group")
                        .callbackData(BALANCE)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Add expense")
                .callbackData(ADD_EXPENSE)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Group members")
                .callbackData(MEMBERS)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Help")
                .callbackData(HELP)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return to main menu")
                .callbackData(BACK_COMMAND)
                .build());
        rows.add(row);

        var text = """
                Welcome to you group %s!
                Choose an option:
                """.formatted(groupService.getGroupName(groupCode));
        sendMessage(chatId, text);
    }

    private void createCommand(Long chatId, Long userId) {
        sendMessage(chatId, "To create a group type in a command /create [group_name]");
    }

    private void handleCreateGroup(Long chatId, Long userId, String input) {
        if (input.equalsIgnoreCase(BACK_BUTTON) || input.equalsIgnoreCase(BACK_COMMAND)) {
            handleBackCommand(chatId, userId);
            return;
        }
        String groupName = input.substring((CREATE + " ").length()).trim();
        try {
            var group = groupService.createGroup(groupName, userId);

            userStateManager.setState(chatId, UserState.DEFAULT);
            var text = """
                    You successfully created a group %s!
                    Invite your friends through this link: https://t.me/Tr1Count_bot?start=invite_%s or through /join %s
                    """.formatted(groupName, group.getId(), group.getId());
            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, group.getId());
            sendMessage(chatId, text);
        } catch (UserNotFoundException e) {
            userStateManager.setState(chatId, UserState.DEFAULT);
            sendMessage(chatId, "Unexpected error while creating group, try again later");
        }
    }

    private void handleBackCommand(Long chatId, Long userId) {
        userStateManager.setState(chatId, UserState.DEFAULT);
        sendMessage(chatId, "You came back to main menu");
    }

    private void handleJoinGroup(Long chatId, Long userId, String input) {
        if (input.equalsIgnoreCase(BACK_BUTTON) || input.equalsIgnoreCase(BACK_COMMAND)) {
            handleBackCommand(chatId, userId);
            return;
        }

        String groupCode = input.substring((JOIN + " ").length()).trim();

        try {
            groupService.joinGroupById(groupCode, userId);

            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, groupCode);
            sendMessage(chatId, "You successfully joined group '" + groupCode + "'.");

        } catch (GroupNotFoundException e) {
            sendMessage(chatId, "Group with code '" + groupCode + "' wasn't found. Try again or press '" + BACK_BUTTON + "'.");
        } catch (UserAlreadyInGroupException e) {
            userStateManager.setState(chatId, UserState.DEFAULT);
            sendMessage(chatId, "You are already a part of this group");
        } catch (Exception e) {
            LOG.error("An unexpected error occurred while joining a group for user " + userId, e);
            sendMessage(chatId, "Unexpected error, try again later");
            userStateManager.setState(chatId, UserState.DEFAULT);
        }
    }

    private void handleInvitation(Long chatId, Long userId, String messageText) {
        String groupId = messageText.substring((START + " invite_").length());

        try {
            groupService.joinGroupById(groupId, userId);
            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, groupId);
            sendMessage(chatId, "You successfully joined group " + groupId + "!");
        } catch (GroupNotFoundException e) {
            sendMessage(chatId, "Group with code " + groupId + " wasn't found. Please check the code or try again");
        } catch (UserAlreadyInGroupException e) {
            sendMessage(chatId, "You are already a part of this group");
        } catch (Exception e) {
            LOG.error("An unexpected error occurred while processing an invitation for user {}", userId, e);
            sendMessage(chatId, "Unexpected error, try again later");
        }
    }

    // Заглушки для других методов, чтобы код был полным
    private void startCommand(Long chatId) {
        sendMessage(chatId, "Hi! I am your TriCount bot.");
    }

    private void unknownCommand(Long chatId) {
        var text = "Unknown command.";
        sendMessage(chatId, text);
    }

    private void joinCommand(Long chatId, Long userId) {
        sendMessage(chatId, "To join a group type in a command '/join [group_code]'.");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(message);
            LOG.debug("Sent message");
        } catch (TelegramApiException e) {
            LOG.error("Error while sending message", e);
        }
    }

    private void helpCommand(Long chatId) {
        var text = """
                Bot description:
                
                This is a TriCount bot! More description coming soon!
                for now use commands:
                /start
                /help
                """;
        sendMessage(chatId, text);
    }

    @Override
    public String getBotUsername() {
        return "TriCount";
    }
}
