package com.IShnitko.Tr1Count_bot.exception;

public class NotEmptyHistoryException extends RuntimeException {
    public NotEmptyHistoryException(String message) {
        super(message);
    }
}
