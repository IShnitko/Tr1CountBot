package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.dto.UpdateExpenseDto;
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

    Expense updateExpense(Long expenseId, UpdateExpenseDto updateExpenseDto);

    String getBalanceText(String groupId);

    List<Expense> getExpensesForGroup(String groupId);

    Expense createExpense(String groupId, CreateExpenseDto dto);

    List<Expense> getPaginatedExpensesForGroup(String groupId, int page);

    String getExpenseTextById(Long expenseId);

    // You might also need this for convenience
    Optional<Expense> getExpenseById(Long expenseId);

    void deleteExpenseById(Long expenseId);
}
