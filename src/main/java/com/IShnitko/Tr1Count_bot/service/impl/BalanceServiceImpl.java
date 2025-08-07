package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.ExpenseShare;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    public Expense addExpenseToGroup(Long groupId, List<User> sharedUsers, Long paidByUserId,
                                     String title, BigDecimal amount, LocalDateTime date) {
        Expense expense = saveExpense(groupId, paidByUserId, title, amount, date); // just saved expense
        int numberOfSharedUsers = sharedUsers.size();
        for (User user : sharedUsers) {
            ExpenseShare es = new ExpenseShare();
            es.setExpense(expense);
            es.setUser(user);
            es.setAmount(amount.divide(BigDecimal.valueOf(numberOfSharedUsers), 2, RoundingMode.DOWN));
            expenseShareRepository.save(es);
        }
        return expense;
    }

    private Expense saveExpense(Long groupId, Long paidByUserId,
                                String title, BigDecimal amount, LocalDateTime date) {
        Expense expense = new Expense();
        expense.setGroup(groupRepository.findGroupById(groupId));
        expense.setAmount(amount);
        expense.setTitle(title);
        expense.setDate(date);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setPaidBy(userRepository.findUserByTelegramId(paidByUserId));

        return expenseRepository.save(expense);
    }

    @Override
    public Map<User, BigDecimal> calculateBalance(Long groupId) {
        Map<User, BigDecimal> balance = new HashMap<>();
        List<User> members = userRepository.findUsersByGroup(groupId);
        for (User user : members) { // finding every user in the group
            List<Expense> eList = expenseRepository.findExpensesByPaidByFromGroup(user, groupId); // find every expense of that user in the certain group
            for (Expense e : eList) {
                // here we take every expense of curr user, add full value of that expense to paidBy user
                // then we take every expenseshare entity of that expense and subtract amount from each user associated with this expense
                // example:
                // paidby user1 90 pln
                // expenseshare user2 30 pln user1 30 pln user3 30 pln
                // balance: user1: +60, user2: -30, user3: -30
                balance.put(user, balance.getOrDefault(user, BigDecimal.valueOf(0)).add(e.getAmount()));
                List<ExpenseShare> esList = expenseShareRepository.findExpenseSharesByExpense(e); // get every es entity associated with this expense
                for (ExpenseShare es : esList) {
                    balance.put(es.getUser(), balance.getOrDefault(es.getUser(), BigDecimal.valueOf(0)).subtract(es.getAmount()));
                }

            }
        }
        return balance;
    }
}
