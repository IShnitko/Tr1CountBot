package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BalanceService {
    Expense addExpenseToGroup(String groupId, List<User> sharedUsers, Long paidByUserId, String title, BigDecimal amount, LocalDateTime date);
    Map<User, BigDecimal> calculateBalance(String groupId);
}
