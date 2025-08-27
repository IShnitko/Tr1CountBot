package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("Join group by code")
                .callbackData(Command.JOIN.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Create group")
                .callbackData(Command.CREATE.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("List groups")
                .callbackData(Command.GROUPS.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Help")
                .callbackData(Command.HELP.getCommand())
                .build());
        rows.add(row);

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup groupMenu() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Balance of the group")
                .callbackData(Command.BALANCE.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Add expense")
                .callbackData(Command.ADD_EXPENSE.getCommand())
                .build());
        row.add(InlineKeyboardButton.builder()
                .text("View expense history")
                .callbackData(Command.HISTORY.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Group members")
                .callbackData(Command.MEMBERS.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Help")
                .callbackData(Command.HELP.getCommand())
                .build());
        row.add(InlineKeyboardButton.builder()
                .text("Invite friends")
                .callbackData(Command.LINK.getCommand())
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return to main menu")
                .callbackData(Command.BACK_COMMAND.getCommand())
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup groupsListMenu(List<Group> groups) {
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

        return inlineKeyboard;
    }

    public InlineKeyboardMarkup returnButton() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return")
                .callbackData(Command.BACK_COMMAND.getCommand())
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup membersMenu(List<User> members, Boolean withDeleteButton) {
        if (members == null || members.isEmpty()) {
            // Если нет участников, вернуть пустую или другую клавиатуру
            return returnButton();
        }

        // Шаг 1: Найти длину самого длинного имени
        int maxLength = members.stream()
                .map(User::getName)
                .mapToInt(String::length)
                .max()
                .orElse(0); // В случае пустого списка, вернет 0

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (User member : members) {
            String name = member.getName();

            int paddingLength = maxLength - name.length();
            String paddedName = name + " ".repeat(paddingLength);

            InlineKeyboardButton nameButton = new InlineKeyboardButton(paddedName);
            nameButton.setCallbackData(Command.INFO.getCommand() + "_" + member.getTelegramId());

            InlineKeyboardButton deleteButton = new InlineKeyboardButton("❌");
            deleteButton.setCallbackData(Command.DELETE.getCommand() + "_" + member.getTelegramId());


            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(nameButton);
            if (withDeleteButton) row.add(deleteButton);

            keyboard.add(row);
        }

        InlineKeyboardButton backButton = new InlineKeyboardButton("↩️ Back to Group Menu");
        backButton.setCallbackData(Command.BACK_COMMAND.getCommand());
        keyboard.add(Collections.singletonList(backButton));

        return new InlineKeyboardMarkup(keyboard);
    }
    public InlineKeyboardMarkup createSharedUsersKeyboard(List<User> members, CreateExpenseDto expenseDto) { // TODO: if no button is pressed then no cross or checkmark is shown
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (expenseDto.getSharedUsers().isEmpty()) {
            expenseDto.initializeSharedUsers(members);
        }

        for (User user : members) {
            // Determine the current checkmark status from the DTO
            boolean isShared = expenseDto.getSharedUsers().getOrDefault(user.getTelegramId(), true);
            String checkmark = isShared ? " ✔️" : " ❌";
            String buttonText = user.getName() + checkmark;

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            // The callback data must contain the user's ID for later processing
            button.setCallbackData("select_shared_user:" + user.getTelegramId());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        // Add a final row with "Confirm" and "Cancel" buttons
        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("✅ Confirm");
        confirmButton.setCallbackData("confirm_shared_users");

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Return");
        cancelButton.setCallbackData(Command.BACK_COMMAND.getCommand());

        List<InlineKeyboardButton> finalRow = new ArrayList<>();
        finalRow.add(confirmButton);
        finalRow.add(cancelButton);
        keyboard.add(finalRow);

        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardMarkup dateButton() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Today")
                .callbackData(Command.DEFAULT_DATE_COMMAND.getCommand())
                .build());
        rows.add(row);
        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return")
                .callbackData(Command.BACK_COMMAND.getCommand())
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup finalConfirmationKeyboard() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("✅ Confirm")
                .callbackData(Command.CONFIRM_SHARED_USERS.getCommand())
                .build());
        rows.add(row);
        row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("❌ Cancel")
                .callbackData(Command.CANCEL_EXPENSE_CREATION.getCommand())
                .build());
        rows.add(row);
        row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("Return")
                .callbackData(Command.BACK_COMMAND.getCommand())
                .build());
        rows.add(row);

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }
}
