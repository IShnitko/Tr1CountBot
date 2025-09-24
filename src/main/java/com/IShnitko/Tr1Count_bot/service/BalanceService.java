package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BalanceService {
    Map<User, BigDecimal> calculateBalance(String groupId);

//    Expense updateExpense(Long expenseId, UpdateExpenseDto updateExpenseDto);

    String getBalanceText(String groupId);

    List<Expense> getExpensesForGroup(String groupId);

    @SuppressWarnings("UnusedReturnValue")
    Expense createExpense(String groupId, CreateExpenseDto dto);

    List<Expense> getPaginatedExpensesForGroup(String groupId, int page);

    String getExpenseTextById(Long expenseId);

    String getExpenseTextFromExpenseDTO(ExpenseUpdateDto expenseUpdateDto);

    Optional<Expense> getExpenseById(Long expenseId);

    void deleteExpenseById(Long expenseId);

    ExpenseUpdateDto buildExpenseUpdateDto(Long chatId, Long expenseId);

    Long saveExpenseUpdateDto(Long chatId);
}
