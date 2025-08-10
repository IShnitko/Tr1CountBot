package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.dto.UpdateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.ExpenseShare;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.ExpenseRepository;
import com.IShnitko.Tr1Count_bot.repository.ExpenseShareRepository;
import com.IShnitko.Tr1Count_bot.repository.GroupRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.util.exception.ExpenseNotFoundException;
import com.IShnitko.Tr1Count_bot.util.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.util.exception.UserNotFoundException;
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
    public Expense addExpenseToGroup(String groupId, List<User> sharedUsers, Long paidByUserId,
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

    private Expense saveExpense(String groupId, Long paidByUserId,
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
    public Map<User, BigDecimal> calculateBalance(String groupId) {
        Map<User, BigDecimal> balance = new HashMap<>();
        List<User> members = userRepository.findUsersByGroup(groupId);
        if (members == null) throw new UserNotFoundException("Can't find members of the group " + groupId);
        for (User user : members) { // finding every user in the group
            List<Expense> eList = expenseRepository.findExpensesByPaidByFromGroup(user, groupId); // find every expense of that user in the certain group
            if (eList == null) throw new ExpenseNotFoundException("Can't find expenses of the user " + user.getTelegramId() + " for group " + groupId);
            for (Expense e : eList) {
                // here we take every expense of curr user, add full value of that expense to paidBy user
                // then we take every expenseshare entity of that expense and subtract amount from each user associated with this expense
                balance.put(user, balance.getOrDefault(user, BigDecimal.valueOf(0)).add(e.getAmount()));
                List<ExpenseShare> esList = expenseShareRepository.findExpenseSharesByExpense(e); // get every es entity associated with this expense
                if (esList == null) throw new ExpenseNotFoundException("Can't find expenseShared for expense " + e.getId());
                for (ExpenseShare es : esList) {
                    balance.put(es.getUser(), balance.getOrDefault(es.getUser(), BigDecimal.valueOf(0)).subtract(es.getAmount()));
                }

            }
        }
        return balance;
    }

    @Override
    public Expense updateExpense(Long expenseId, UpdateExpenseDto updateDto) {
        Expense expenseToUpdate = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense wasn't found"));

        updateDto.paidByUserId().ifPresent(newPaidByUserId -> {
            User newPayer = userRepository.findUserByTelegramId(newPaidByUserId);
            expenseToUpdate.setPaidBy(newPayer);
        });

        updateDto.title().ifPresent(expenseToUpdate::setTitle);

        updateDto.amount().ifPresent(expenseToUpdate::setAmount);

        updateDto.date().ifPresent(expenseToUpdate::setDate);

        updateDto.newSharedUsers().ifPresent(newSharedUsers -> {
            List<ExpenseShare> oldShares = expenseShareRepository.findExpenseSharesByExpense(expenseToUpdate);
            expenseShareRepository.deleteAll(oldShares);

            BigDecimal totalAmount = expenseToUpdate.getAmount();
            int numberOfSharedUsers = newSharedUsers.size();

            if (numberOfSharedUsers > 0) {
                BigDecimal shareAmount = totalAmount.divide(BigDecimal.valueOf(numberOfSharedUsers), 2, RoundingMode.DOWN);

                for (User user : newSharedUsers) {
                    ExpenseShare es = new ExpenseShare();
                    es.setExpense(expenseToUpdate);
                    es.setUser(user);
                    es.setAmount(shareAmount);
                    expenseShareRepository.save(es);
                }
            }
        });

        return expenseRepository.save(expenseToUpdate);
    }

    @Override
    public List<Expense> getExpensesForGroup(String groupId) {
        if (groupRepository.findGroupById(groupId) == null) throw new GroupNotFoundException("Can't find expenses for group " + groupId + ", because it doesn't exist");
        return expenseRepository.getExpensesByGroup(groupRepository.findGroupById(groupId));
    }
}
