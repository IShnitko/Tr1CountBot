package com.IShnitko.Tr1Count_bot.util;

import java.security.SecureRandom;

/**
 * Утилитарный класс для генерации случайных кодов для групп.
 */
public final class GroupCodeGenerator {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates code for groups
     *
     * @param length code length
     * @return generated code
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Длина кода должна быть больше 0");
        }

        StringBuilder codeBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // Выбираем случайный символ из строки CHARACTERS
            int randomIndex = random.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}
