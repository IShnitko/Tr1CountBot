package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.service.impl.UserInteractionServiceImpl;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.exception.CreatorDeletionException;
import com.IShnitko.Tr1Count_bot.exception.UserNotFoundException;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@StateHandlerFor(UserState.MEMBERS_MENU)
@RequiredArgsConstructor
public class MembersMenuHandler  implements StateHandler {

    private final MessageServiceImpl messageService;
    private final UserStateManager userStateManager;
    private final GroupService groupService;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getCallbackData();

        if (context.getCallbackQueryId() != null) { // SAFETY CHECK
            messageService.answerCallbackQuery(context.getCallbackQueryId());
        }
        Command command = Command.fromString(input.split("_")[0]);
        String groupCode = userStateManager.getChosenGroup(context.getChatId());
        switch (command) {
            case INFO -> getMemberInfo(context, input);
            case DELETE -> deleteMember(context, groupCode, input);
            case BACK_COMMAND -> returnToGroup(context, groupCode);
            default -> messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
        }
    }

    private void deleteMember(ChatContext context, String groupCode, String input) {
        Long userId = Long.valueOf(input.split("_")[1]);
        try {
            groupService.deleteUserFromGroup(groupCode, userId);
            userStateManager.setState(context.getChatId(), UserState.IN_THE_GROUP);
            groupManagementService.displayGroup(context.getChatId(), groupCode, null, context.getMessage().getMessageId(), "Successfully deleted group member");
        } catch (UserNotFoundException e) {
            userStateManager.setState(context.getChatId(), UserState.IN_THE_GROUP);
            groupManagementService.displayGroup(context.getChatId(),
                    groupCode,
                    null,
                    context.getMessage().getMessageId(),
                    "Error occurred while deleting this user, try again later!!");
        } catch (CreatorDeletionException e) {
            userStateManager.setState(context.getChatId(), UserState.IN_THE_GROUP);
            groupManagementService.displayGroup(context.getChatId(),
                    groupCode,
                    null,
                    context.getMessage().getMessageId(),
                    "Can't delete creator of the group!");
        }
    }

    private void getMemberInfo(ChatContext context, String input) {
        Long userId = Long.valueOf(input.split("_")[1]);
        groupManagementService.viewUserInfo(context.getChatId(), context.getMessage().getMessageId(), userId);
        userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_MEMBERS_MENU);
    }

    private void returnToGroup(ChatContext context, String groupCode) {
        userStateManager.setState(context.getChatId(), UserState.IN_THE_GROUP);
        groupManagementService.displayGroup(context.getChatId(), groupCode, context.getMessage().getMessageId());
    }
}
