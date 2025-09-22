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
    CONFIRM_SHARED_USERS("confirm_shared_users"),
    CANCEL_EXPENSE_CREATION("cancel_expense_creation"), // TODO: change with /can...
    DEFAULT_DATE_COMMAND("today"),
    EDIT("/edit"),
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
