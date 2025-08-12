package com.IShnitko.Tr1Count_bot.util;

import java.security.SecureRandom;

/**
 * Утилитарный класс для генерации случайных кодов для групп.
 */
public class GroupCodeGenerator {

    // Набор символов для генерации кода: заглавные буквы и цифры.
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // TODO: maybe add special symbols and lowercase letters

    // Используем SecureRandom для криптографически надежной генерации случайных чисел.
    // Это лучший выбор, чем обычный Random, особенно если уникальность критически важна.
    private static final SecureRandom random = new SecureRandom();

    /**
     * Генерирует случайный код заданной длины из заглавных букв и цифр.
     *
     * @param length Желаемая длина кода (например, 5 для "YFPSJ").
     * @return Сгенерированный код.
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
