package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.util.exception.UserAlreadyInGroupException;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();

            // Получаем текущее состояние пользователя
            UserState currentState = userStateManager.getState(chatId);

            // Обрабатываем ввод в зависимости от состояния
            if (currentState == UserState.AWAITING_GROUP_CODE) {
                handleJoinGroup(chatId, userId, messageText);
            } else {
                // Если состояние - DEFAULT, обрабатываем как обычную команду
                if (messageText.startsWith(START + " invite_")) {
                    handleInvitation(chatId, userId, messageText);
                } else {
                    switch (messageText) {
                        case START:
                            startCommand(chatId);
                            break;
                        case HELP:
                            helpCommand(chatId);
                            break;
                        case JOIN:
                            joinCommand(chatId, userId);
                            break;
                        case BACK_COMMAND:
                            handleBackCommand(chatId, userId);
                            break;
                        case CREATE:
                            createCommand(chatId, userId);
                        // TODO: implement other commands
                        default:
                            unknownCommand(chatId);
                            break;
                    }
                }
            }
        }
    }

    private void createCommand(Long chatId, Long userId) {
        sendMessage(chatId, "To create a group type in a command /create [group_name]");
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

        String groupCode = input.trim();
        try {
            groupService.joinGroupByInvitation(groupCode, userId);

            userStateManager.setState(chatId, UserState.DEFAULT);
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
        String invitationCode = messageText.substring((START + " invite_").length());

        try {
            groupService.joinGroupByInvitation(invitationCode, userId);
            sendMessage(chatId, "You successfully joined group " + invitationCode + "!");
        } catch (GroupNotFoundException e) {
            sendMessage(chatId, "Group with code. Please check the code or try again");
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
