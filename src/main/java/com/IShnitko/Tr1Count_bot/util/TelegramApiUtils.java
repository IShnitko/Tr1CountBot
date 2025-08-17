package com.IShnitko.Tr1Count_bot.util;

public final class TelegramApiUtils {

    // Private constructor to prevent instantiation
    private TelegramApiUtils() {
    }

    /**
     * Escapes special characters in a string for MarkdownV2 formatting.
     * The following characters are escaped: _, *, [, ], (, ), ~, `, >, #, +, -, =, |, {, }, ., !
     *
     * @param text The input string to format.
     * @return The formatted string with special characters escaped.
     */
    public static String formatString(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // List of characters that need to be escaped in MarkdownV2
        final String[] specialCharacters = new String[]{
                "_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!"
        };

        StringBuilder formattedText = new StringBuilder(text);

        // Escape each special character by adding a backslash before it
        for (String character : specialCharacters) {
            int index = formattedText.indexOf(character);
            while (index != -1) {
                formattedText.insert(index, "\\");
                index = formattedText.indexOf(character, index + 2);
            }
        }

        return formattedText.toString();
    }
}
