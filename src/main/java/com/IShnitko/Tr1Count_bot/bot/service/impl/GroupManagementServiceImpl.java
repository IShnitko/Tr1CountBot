package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.impl.UserServiceImpl;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class GroupManagementServiceImpl implements GroupManagementService {
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final UserServiceImpl userService;


    @Override
    public void groupHelpCommand(Long chatId, Integer messageId) {
        String text = """
                💡 *Group Help*
                
                Available commands:
                - /balance: Show current balances
                - /add_expense: Record new expense
                - /members: List group members
                - /help: Show this message
                - /back: Return to main menu
                """;
        messageService.editMessage(chatId, messageId, text, keyboardFactory.returnButton());
    }

    @Override
    public void displayGroup(Long chatId, String groupCode, Integer botMessageId, Integer inputMessageId) {
        if (inputMessageId != null) messageService.deleteMessage(chatId, inputMessageId);
//        messageService.deleteMessage(chatId, inputMessageId);
        if (botMessageId == null)
            messageService.sendMessage(chatId, "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        else {
            messageService.editMessage(chatId, botMessageId,
                    "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        }

    }

    @Override
    public void displayGroup(Long chatId, String groupCode, Integer botMessageId, Integer inputMessageId, String additionalText) {
        if (botMessageId == null) {
            messageService.editMessage(chatId,
                    inputMessageId,
                    additionalText + "\n\nThis is your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        } else {
            messageService.deleteMessage(chatId, inputMessageId);
            messageService.editMessage(chatId,
                    botMessageId,
                    additionalText + "\n\nThis is your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        }
    }

    @Override
    public void displayGroup(Long chatId, String groupCode, Integer botMessageId) {
        if (botMessageId == null)
            messageService.sendMessage(chatId, "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        else {
            messageService.editMessage(chatId, botMessageId,
                    "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        }
    }

    @Override
    public void sendIncorrectGroupId(Long chatId, Integer messageId, @NonNull Long userId) {
        messageService.deleteMessage(chatId, messageId);
        List<Group> groups = groupService.getGroupsForUser(userId);
        Integer botMessageId = userStateManager.getBotMessageId(chatId);

        String messageText;
        if (!groups.isEmpty()) {
            messageText = "Incorrect group ID. Try again or choose a group below:";
            messageService.editMessage(chatId, botMessageId, messageText, keyboardFactory.groupsListMenu(groups));
        } else {
            messageText = "Incorrect group ID. Try again:";
            messageService.editMessage(chatId, botMessageId, messageText, keyboardFactory.returnButton());
        }
    }

    @Override
    public void sendJoinLink(Long chatId, Integer messageId) {
        String groupId = userStateManager.getChosenGroup(chatId);

        String joinLink = String.format("https://t.me/Tr1Count_bot?start=invite_%s", groupId);
        String message = "🔗 *Here is the link to share with your friends to join the group\\:* \n\n" +
                joinLink;

        messageService.editMessage(chatId, messageId, message, keyboardFactory.returnButton());

    }

    @Override
    public void viewUserInfo(Long chatId, Integer messageId, Long userId) {
        messageService.editMessage(chatId,
                messageId,
                userService.getUserInfoForGroup(userId,
                        userStateManager.getChosenGroup(chatId)),
                keyboardFactory.returnButton()
        );
    }

    @Override
    public void viewMembersMenu(Long chatId, Integer messageId, Long userId) {
        String groupId = userStateManager.getChosenGroup(chatId);
        try {
            List<User> members = groupService.getUsersForGroup(groupId);
            if (Objects.equals(userService.getCreatorOfTheGroup(groupId), userId)) {
                messageService.editMessage(chatId, messageId,"👥 *Group Members*\n\n", keyboardFactory.membersMenu(members, true));
            } else {
                messageService.editMessage(chatId, messageId,"👥 *Group Members*\n\n", keyboardFactory.membersMenu(members, false));
            }
            userStateManager.setState(chatId, UserState.MEMBERS_MENU);
        } catch (Exception e) {
            messageService.editMessage(chatId, messageId,"❌ Error retrieving members", keyboardFactory.returnButton());
        }
    }
}
