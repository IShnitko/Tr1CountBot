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
        expense.setGroup(groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Expense can't be saved because group " + groupId + " doesn't exist")));
        expense.setAmount(amount);
        expense.setTitle(title);
        expense.setDate(date);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setPaidBy(userRepository.findUserByTelegramId(paidByUserId)
                .orElseThrow(() -> new UserNotFoundException("Can't set paidBy value for expense, because user " + paidByUserId + " doesn't exist")));

        return expenseRepository.save(expense);
    }

    /**
     * Correctly calculates the balance for all members of a group.
     *
     * @param groupId the unique identifier of the group
     * @return a Map where the key is the group member and the value is their final balance
     */
    @Override
    public Map<User, BigDecimal> calculateBalance(String groupId) {
        // Get all members of the group
        List<User> members = userRepository.findUsersByGroup(groupId);
        if (members.isEmpty()) {
            throw new UserNotFoundException("Can't find members of the group " + groupId);
        }

        // Initialize the balance for all members to zero.
        // This ensures that every member will be displayed in the final balance,
        // even if they have not spent or participated in anything.
        Map<User, BigDecimal> balance = new HashMap<>();
        for (User user : members) {
            balance.put(user, BigDecimal.ZERO);
        }

        // Get all expenses related to this group
        // This is much more efficient than querying inside a loop
        List<Expense> allExpenses = expenseRepository.findExpensesByGroup(
                groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Can't find expenses for the group " + groupId)));

        // Process each expense
        for (Expense expense : allExpenses) {
            // Add the full amount of the expense to the user who paid
            User paidBy = expense.getPaidBy();
            balance.computeIfPresent(paidBy, (user, currentBalance) -> currentBalance.add(expense.getAmount()));

            // Get the shares for the current expense
            List<ExpenseShare> expenseShares = expenseShareRepository.findExpenseSharesByExpense(expense);

            // Subtract the share amount from each participating user
            for (ExpenseShare share : expenseShares) {
                User user = share.getUser();
                balance.computeIfPresent(user, (u, currentBalance) -> currentBalance.subtract(share.getAmount()));
            }
        }

        return balance;
    }


    @Override
    public Expense updateExpense(Long expenseId, UpdateExpenseDto updateDto) {
        Expense expenseToUpdate = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense wasn't found"));

        updateDto.paidByUserId().ifPresent(newPaidByUserId -> {
            User newPayer = userRepository.findUserByTelegramId(newPaidByUserId)
                    .orElseThrow(() -> new UserNotFoundException(String.format("Can't update expense %s, because newPayer %s doesn't exist", expenseId, newPaidByUserId)));
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
        return expenseRepository.getExpensesByGroup(groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Can't find expenses for group " + groupId + ", because it doesn't exist")));
    }
}
