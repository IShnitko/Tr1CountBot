package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BalanceService {
    Group addExpenseToGroup(Long groupId, List<User> sharedUsers, Long paidByUserId, String title, BigDecimal amount, LocalDateTime date);
    Map<User, BigDecimal> calculateBalance(Long groupId);
    Map<User, BigDecimal> calculateHowToPay(Long groupId);
}
