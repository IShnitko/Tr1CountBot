package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.ExpenseRepository;
import com.IShnitko.Tr1Count_bot.repository.ExpenseShareRepository;
import com.IShnitko.Tr1Count_bot.repository.GroupRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public BalanceServiceImpl(ExpenseShareRepository expenseShareRepository, ExpenseRepository expenseRepository, GroupRepository groupRepository, UserRepository userRepository) {
        this.expenseShareRepository = expenseShareRepository;
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Group addExpenseToGroup(Long groupId, List<User> sharedUsers, Long paidByUserId,
                                   String title, BigDecimal amount, LocalDateTime date) {
        return null;
    }

    @Override
    public Map<User, BigDecimal> calculateBalance(Long groupId) {
        return Map.of();
    }

    @Override
    public Map<User, BigDecimal> calculateHowToPay(Long groupId) {
        return Map.of();
    }
}
