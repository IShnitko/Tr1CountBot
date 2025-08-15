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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public BalanceServiceImpl(ExpenseShareRepository expenseShareRepository,
                              ExpenseRepository expenseRepository,
                              GroupRepository groupRepository,
                              UserRepository userRepository) {
        this.expenseShareRepository = expenseShareRepository;
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Expense addExpenseToGroup(String groupId, List<User> sharedUsers, Long paidByUserId,
                                     String title, BigDecimal amount, LocalDateTime date) {
        // Валидация
        if (sharedUsers == null || sharedUsers.isEmpty()) {
            throw new IllegalArgumentException("Shared users list cannot be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive");
        }

        // Сохранение расхода
        Expense expense = saveExpense(groupId, paidByUserId, title, amount, date);

        // Расчет и сохранение долей
        BigDecimal shareAmount = amount.divide(
                BigDecimal.valueOf(sharedUsers.size()),
                2,
                RoundingMode.HALF_EVEN
        );

        List<ExpenseShare> shares = new ArrayList<>();
        for (User user : sharedUsers) {
            ExpenseShare es = new ExpenseShare();
            es.setExpense(expense);
            es.setUser(user);
            es.setAmount(shareAmount);
            shares.add(es);
        }
        expenseShareRepository.saveAll(shares);

        return expense;
    }

    private Expense saveExpense(String groupId, Long paidByUserId,
                                String title, BigDecimal amount, LocalDateTime date) {
        Group group = groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));

        User paidBy = userRepository.findUserByTelegramId(paidByUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + paidByUserId));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setDate(date != null ? date : LocalDateTime.now());
        expense.setCreatedAt(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<User, BigDecimal> calculateBalance(String groupId) {
        // Получаем участников группы
        List<User> members = userRepository.findUsersByGroup(groupId);
        if (members.isEmpty()) {
            return Collections.emptyMap();
        }

        // Инициализация баланса
        Map<User, BigDecimal> balance = new HashMap<>();
        for (User user : members) {
            balance.put(user, BigDecimal.ZERO);
        }

        // Получаем все расходы группы
        List<Expense> expenses = expenseRepository.findExpensesByGroupId(groupId);

        // Получаем все доли расходов для группы
        List<Long> expenseIds = expenses.stream().map(Expense::getId).collect(Collectors.toList());
        List<ExpenseShare> allShares = expenseShareRepository.findByExpenseIdIn(expenseIds);

        // Группируем доли по расходам
        Map<Long, List<ExpenseShare>> sharesByExpense = allShares.stream()
                .collect(Collectors.groupingBy(share -> share.getExpense().getId()));

        // Рассчитываем баланс
        for (Expense expense : expenses) {
            // Увеличиваем баланс плательщика
            User paidBy = expense.getPaidBy();
            balance.put(paidBy, balance.get(paidBy).add(expense.getAmount()));

            // Уменьшаем баланс участников
            List<ExpenseShare> shares = sharesByExpense.get(expense.getId());
            if (shares != null) {
                for (ExpenseShare share : shares) {
                    User user = share.getUser();
                    balance.put(user, balance.get(user).subtract(share.getAmount()));
                }
            }
        }

        return balance;
    }

    @Override
    @Transactional
    public Expense updateExpense(Long expenseId, UpdateExpenseDto updateDto) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found: " + expenseId));

        // Обновление плательщика
        updateDto.paidByUserId().ifPresent(paidByUserId -> {
            User newPayer = userRepository.findUserByTelegramId(paidByUserId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + paidByUserId));
            expense.setPaidBy(newPayer);
        });

        // Обновление заголовка
        updateDto.title().ifPresent(expense::setTitle);

        // Обновление суммы
        updateDto.amount().ifPresent(amount -> {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            expense.setAmount(amount);
        });

        // Обновление даты
        updateDto.date().ifPresent(expense::setDate);

        // Обновление списка участников
        updateDto.newSharedUsers().ifPresent(newSharedUsers -> {
            if (newSharedUsers.isEmpty()) {
                throw new IllegalArgumentException("Shared users cannot be empty");
            }

            // Удаляем старые доли
            expenseShareRepository.deleteByExpenseId(expenseId);

            // Рассчитываем новую долю на каждого участника
            BigDecimal shareAmount = expense.getAmount().divide(
                    BigDecimal.valueOf(newSharedUsers.size()),
                    2,
                    RoundingMode.HALF_EVEN
            );

            // Создаём новые доли
            List<ExpenseShare> newShares = new ArrayList<>();
            for (User user : newSharedUsers) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUser(user);
                share.setAmount(shareAmount);
                newShares.add(share);
            }
            expenseShareRepository.saveAll(newShares);
        });

        return expenseRepository.save(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesForGroup(String groupId) {
        return expenseRepository.findExpensesByGroupId(groupId);
    }

    @Override
    public String getBalanceText(String groupId) {
        Map<User, BigDecimal> balance = calculateBalance(groupId);

        if (balance.isEmpty()) {
            return "No balance data available for this group";
        }

        StringBuilder sb = new StringBuilder("💰 *Group Balance* 💰\n\n");
        balance.forEach((user, amount) -> {
            String emoji = amount.compareTo(BigDecimal.ZERO) >= 0 ? "↑" : "↓";
            sb.append(String.format("%s %s: %s\n", emoji, user.getName(), amount));
        });

        return sb.toString();
    }
}