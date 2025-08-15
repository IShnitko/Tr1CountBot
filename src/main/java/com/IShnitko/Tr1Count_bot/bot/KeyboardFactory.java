package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.*;

@Component
public class KeyboardFactory {
    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("Join group by code")
                .callbackData(JOIN)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Create code")
                .callbackData(CREATE)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("List groups")
                .callbackData(GROUPS)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Help")
                .callbackData(HELP)
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
                .text("Return to main menu")
                .callbackData(BACK_COMMAND)
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup membersMenu(List<User> members) {
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
            nameButton.setCallbackData(INFO + "_" + member.getTelegramId());

            InlineKeyboardButton deleteButton = new InlineKeyboardButton("❌");
            deleteButton.setCallbackData(DELETE + "_" + member.getTelegramId());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(nameButton);
            row.add(deleteButton);

            keyboard.add(row);
        }

        InlineKeyboardButton backButton = new InlineKeyboardButton("↩️ Back to Group Menu");
        backButton.setCallbackData(BACK_COMMAND);
        keyboard.add(Collections.singletonList(backButton));

        return new InlineKeyboardMarkup(keyboard);
    }
}
