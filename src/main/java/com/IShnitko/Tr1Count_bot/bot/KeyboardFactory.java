package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
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

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return to main menu")
                .callbackData(BACK_COMMAND)
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
}
