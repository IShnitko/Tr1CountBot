package com.IShnitko.Tr1Count_bot.bot.service;

public interface ExpenseManagementService {
    void startAddingExpense(Long chatId, Integer messageId);

    void sendInvalidStartAddingExpense(Long chatId, Integer messageId);

    void sendDateInput(Long chatId, Integer inputMessageId);

    void sendInvalidDateInput(Long chatId, Integer messageId);

    void sendPaidBy(Long chatId, Integer messageId);

    void sendSharedUsers(Long chatId);

    void sendIncorrectSharedUsers(Long chatId);

    void sendSummary(Long chatId);

    void sendExpenseHistoryView(Long chatId, Integer messageId, int page);

    void sendExpenseHistoryView(Long chatId, Integer messageId, int page, String additionalText);

    void sendDeleteConfirmation(Long chatId, Integer messageId, Long expenseIdToDelete);

}
