package com.IShnitko.Tr1Count_bot.bot.handlers;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.util.exception.UserAlreadyInGroupException;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
            case BACK_COMMAND -> userInteractionService.handleBackCommand(context.getChatId());
            case CREATE -> handleCreate(context);
            case GROUPS -> chooseGroup(context);
            default -> userInteractionService.unknownCommand(context.getChatId());
        }
    }

    private void handleStart(ChatContext context, String input) {
        if (input.startsWith(START + " invite_")) {
            handleInvitation(context, input);
        } else {
            userInteractionService.startCommand(context.getChatId());
        }
    }

    private void handleInvitation(ChatContext context, String input) {
        String groupId = input.substring((START + " invite_").length());
        try {
            groupService.joinGroupById(groupId, context.getUser().getId());
            userStateManager.setStateWithChosenGroup(context.getChatId(), UserState.IN_THE_GROUP, groupId);
            messageService.sendMessage(context.getChatId(), "You successfully joined group " + groupId + "!");
            groupManagementService.displayGroup(context.getChatId(), groupId);
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
            messageService.sendMessage(context.getChatId(), "Enter group code:");
            // Здесь можно установить состояние ожидания кода
        }
    }

    private void joinGroup(ChatContext context, String groupCode) {
        try {
            groupService.joinGroupById(groupCode, context.getUser().getId());
            userStateManager.setStateWithChosenGroup(context.getChatId(), UserState.IN_THE_GROUP, groupCode);
            messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
            messageService.sendMessage(context.getChatId(), "You joined group: " + groupCode);
            groupManagementService.displayGroup(context.getChatId(), groupCode);
        } catch (GroupNotFoundException e) {
            messageService.sendMessage(context.getChatId(), "Group not found: " + groupCode);
        } catch (UserAlreadyInGroupException e) {
            messageService.sendMessage(context.getChatId(), "You're already in this group");
        }
    }

    private void handleCreate(ChatContext context) {
        messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
        messageService.sendMessage(context.getChatId(), "Enter group name:", keyboardFactory.returnButton());
        userStateManager.setState(context.getChatId(), UserState.AWAITING_GROUP_NAME);
    }

    private void chooseGroup(ChatContext context) {
        List<Group> groups = groupService.getGroupsForUser(context.getUser().getId());
        if (groups.isEmpty()) {
            messageService.sendMessage(context.getChatId(), "You're not in any groups yet!");
        } else {
            InlineKeyboardMarkup keyboard = keyboardFactory.groupsListMenu(groups);
            messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
            messageService.sendMessage(context.getChatId(), "Choose a group:", keyboard);
            userStateManager.setState(context.getChatId(), UserState.AWAITING_GROUP_ID); // TODO: answer callback
        }
    }

}