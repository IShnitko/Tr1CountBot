package com.IShnitko.Tr1Count_bot.bot.model;

import lombok.Getter;

@Getter
public enum Command {
    START("/start"),
    HELP("/help"),
    JOIN("/join"),
    CREATE("/create"),
    GROUPS("/groups"),
    BALANCE("/balance"),
    ADD_EXPENSE("/add_expense"),
    MEMBERS("/members"),
    BACK_COMMAND("/back"),
    DELETE("/delete"),
    HISTORY("/history"),
    LINK("/link"),
    INFO("/info"),
    CONFIRM("/confirm"),
    CANCEL("/cancel"),
    DEFAULT_DATE_COMMAND("/today"),
    EDIT_TITLE("/edit_title"),
    EDIT_AMOUNT("/edit_amount"),
    EDIT_PAID_BY("/edit_paid_by"),
    EDIT_DATE("/edit_date"),
    EDIT_SHARED_WITH("/edit_shared_with"),
    PREV_PAGE("prev_page"),
    NEXT_PAGE("/next_page");

    private final String command;

    Command(String command) {
        this.command = command;
    }

    /**
     * Returns the corresponding enum for a given command string.
     * If the command is unknown, returns null.
     */
    public static Command fromString(String command) {
        for (Command c : Command.values()) {
            if (c.command.equalsIgnoreCase(command)) {
                return c;
            }
        }
        return null;
    }
}
