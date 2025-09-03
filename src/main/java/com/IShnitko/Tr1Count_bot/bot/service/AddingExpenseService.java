package com.IShnitko.Tr1Count_bot.bot.service;

import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;

public interface AddingExpenseService {
    void startAddingExpense(Long chatId, Integer messageId);

    void sendInvalidStartAddingExpense(Long chatId, Integer messageId);

    void sendDateInput(Long chatId, Integer inputMessageId);

    void sendInvalidDateInput(Long chatId, Integer messageId);

    void sendPaidBy(Long chatId, Integer messageId);

    void sendSharedUsers(Long chatId);

    void sendIncorrectSharedUsers(Long chatId);

    void sendSummary(Long chatId);

}
