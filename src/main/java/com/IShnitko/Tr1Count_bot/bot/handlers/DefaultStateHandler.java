package com.IShnitko.Tr1Count_bot.bot.handlers;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.exception.UserAlreadyInGroupException;
import com.IShnitko.Tr1Count_bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.*;

@Component
@StateHandlerFor(UserState.DEFAULT)
@RequiredArgsConstructor
public class DefaultStateHandler implements StateHandler {
    private final MessageService messageService;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;

    private final GroupService groupService;
    private final UserStateManager userStateManager;
    private final KeyboardFactory keyboardFactory;

    @Override
    public void handle(ChatContext context) {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        if (input == null) {
            userInteractionService.unknownCommand(context.getChatId());
            return;
        }
        if (context.getCallbackQueryId() != null) { // SAFETY CHECK
            messageService.answerCallbackQuery(context.getCallbackQueryId());
        }
        String command = input.split(" ")[0];
        switch (command) {
            case START -> handleStart(context, input);
            case HELP -> userInteractionService.helpCommand(context.getChatId());
            case JOIN -> handleJoin(context, input);
            case CREATE -> handleCreate(context);
            case GROUPS -> chooseGroup(context);
            default -> userInteractionService.unknownCommand(context.getChatId());
        }
    }

    private void handleStart(ChatContext context, String input) {
        if (input.startsWith(START + " invite_")) {
            handleInvitation(context, input);
        } else {
            userInteractionService.startCommand(context.getChatId(), null);
        }
    }

    private void handleInvitation(ChatContext context, String input) {
        String groupId = input.substring((START + " invite_").length());
        try {
            groupService.joinGroupById(groupId, context.getUser().getId());
            userStateManager.setStateWithChosenGroup(context.getChatId(), UserState.IN_THE_GROUP, groupId);
            groupManagementService.displayGroup(context.getChatId(), groupId, null);
        } catch (GroupNotFoundException e) {
            messageService.sendMessage(context.getChatId(), "Group not found: " + groupId);
        } catch (UserAlreadyInGroupException e) {
            messageService.sendMessage(context.getChatId(), "You're already in this group");
        }
    }

    private void handleJoin(ChatContext context, String input) {
        if (context.getUpdateType() == ChatContext.UpdateType.MESSAGE && input.length() > JOIN.length()) {
            String groupCode = input.substring(JOIN.length()).trim();
            joinGroup(context, groupCode);
        } else {
            userStateManager.setBotMessageId(context.getChatId(),
                    messageService.editMessage(context.getChatId(),
                            context.getMessage().getMessageId(),
                            "Enter group code:",
                            keyboardFactory.returnButton()));
            userStateManager.setState(context.getChatId(), UserState.AWAITING_GROUP_ID);
        }
    }

    private void joinGroup(ChatContext context, String groupCode) {
        try {
            groupService.joinGroupById(groupCode, context.getUser().getId());
            userStateManager.setStateWithChosenGroup(context.getChatId(), UserState.IN_THE_GROUP, groupCode);
            groupManagementService.displayGroup(context.getChatId(), groupCode, context.getMessage().getMessageId());
        } catch (GroupNotFoundException e) {
            messageService.sendMessage(context.getChatId(), "Group not found: " + groupCode);
        } catch (UserAlreadyInGroupException e) {
            messageService.sendMessage(context.getChatId(), "You're already in this group");
        }
    }

    private void handleCreate(ChatContext context) {
        messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(), "Enter group name:", keyboardFactory.returnButton());
        userStateManager.setState(context.getChatId(), UserState.AWAITING_GROUP_NAME);
    }

    private void chooseGroup(ChatContext context) {
        List<Group> groups = groupService.getGroupsForUser(context.getUser().getId());
        Long chatId = context.getChatId();
        if (groups.isEmpty()) {
            messageService.sendMessage(chatId, "You're not in any groups yet!");
        } else {
            messageService.editMessage(chatId, context.getMessage().getMessageId(), "Choose a group:", keyboardFactory.groupsListMenu(groups));
            userStateManager.setState(chatId, UserState.AWAITING_GROUP_ID);
            userStateManager.setBotMessageId(chatId, context.getMessage().getMessageId());
        }
    }

}