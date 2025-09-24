package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.ExpenseShare;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.ExpenseRepository;
import com.IShnitko.Tr1Count_bot.repository.ExpenseShareRepository;
import com.IShnitko.Tr1Count_bot.repository.GroupRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.exception.ExpenseNotFoundException;
import com.IShnitko.Tr1Count_bot.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BalanceServiceImpl implements BalanceService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // Self-injection to ensure @Transactional works on self-invocations
    private final BalanceService self;
    private final UserStateManager userStateManager;

    @Autowired
    public BalanceServiceImpl(ExpenseShareRepository expenseShareRepository,
                              ExpenseRepository expenseRepository,
                              GroupRepository groupRepository, GroupRepository groupRepository1,
                              UserRepository userRepository,
                              @Lazy BalanceService self, UserStateManager userStateManager) {
        this.expenseShareRepository = expenseShareRepository;
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository1;
        this.userRepository = userRepository;
        this.self = self;
        this.userStateManager = userStateManager;
    }

    /**
     * Creates and saves a new expense to a group based on the data in a CreateExpenseDto.
     * This method is a more streamlined replacement for addExpenseToGroup.
     *
     * @param groupId The ID of the group where the expense will be added.
     * @param dto     The DTO containing all the expense details.
     * @return The newly created Expense entity.
     */
    @Transactional
    public Expense createExpense(String groupId, CreateExpenseDto dto) {
        // Validation checks
        if (dto.getSharedUsers() == null || dto.getSharedUsers().isEmpty()) {
            throw new IllegalArgumentException("Shared users list cannot be empty");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive");
        }

        // Fetch users from the database based on the DTO's IDs
        List<User> sharedUsers = userRepository.findAllByTelegramIdIn(dto.getSharedUsers().keySet());
        if (sharedUsers.size() != dto.getSharedUsers().size()) {
            throw new IllegalArgumentException("One or more shared users not found in the database.");
        }

        // Save the main expense
        Expense expense = saveExpense(
                groupId,
                dto.getPaidByUserId(),
                dto.getTitle(),
                dto.getAmount(),
                dto.getDate()
        );

        // Calculate and save shares
        BigDecimal shareAmount = dto.getAmount().divide(
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
        List<User> members = userRepository.findUsersByGroup(groupId);
        if (members.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<User, BigDecimal> balance = new HashMap<>();
        for (User user : members) {
            balance.put(user, BigDecimal.ZERO);
        }

        List<Expense> expenses = expenseRepository.findExpensesByGroupId(groupId);

        List<Long> expenseIds = expenses.stream().map(Expense::getId).collect(Collectors.toList());
        List<ExpenseShare> allShares = expenseShareRepository.findByExpenseIdIn(expenseIds);

        Map<Long, List<ExpenseShare>> sharesByExpense = allShares.stream()
                .collect(Collectors.groupingBy(share -> share.getExpense().getId()));

        for (Expense expense : expenses) {
            User paidBy = expense.getPaidBy();
            balance.put(paidBy, balance.get(paidBy).add(expense.getAmount()));

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
    @Transactional(readOnly = true)
    public List<Expense> getExpensesForGroup(String groupId) {
        return expenseRepository.findExpensesByGroupId(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getPaginatedExpensesForGroup(String groupId, int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("date").descending());
        return expenseRepository.findByGroupIdOrderByDateDesc(groupId, pageable).stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String getExpenseTextById(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found with ID: " + expenseId));

        List<ExpenseShare> shares = expenseShareRepository.findByExpenseId(expenseId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        StringBuilder sb = new StringBuilder();
        sb.append("💸 *Expense Details* 💸\n\n");
        sb.append(String.format("📝 *Title:* %s\n", expense.getTitle()));
        sb.append(String.format("💵 *Amount:* `%.2f`\n", expense.getAmount()));
        sb.append(String.format("👤 *Paid by:* %s\n", expense.getPaidBy().getName()));
        sb.append(String.format("🗓️ *Date:* %s\n\n", expense.getDate().format(formatter)));

        sb.append("👥 *Shared with:*\n");
        for (ExpenseShare share : shares) {
            sb.append(String.format("  - %s: `%.2f`\n", share.getUser().getName(), share.getAmount()));
        }


        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String getExpenseTextFromExpenseDTO(ExpenseUpdateDto expenseUpdateDto) {
        log.info("Stared creating text for expense DTO");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        StringBuilder sb = new StringBuilder();
        sb.append("💸 *Expense Details* 💸\n\n");
        sb.append(String.format("📝 *Title:* %s\n", expenseUpdateDto.getTitle()));
        sb.append(String.format("💵 *Amount:* `%.2f`\n", expenseUpdateDto.getAmount()));
        sb.append(String.format("👤 *Paid by:* %s\n",
                userRepository.findUserByTelegramId(
                        expenseUpdateDto.getPaidByUserId()
                ).orElseThrow(() -> new UserNotFoundException("Can't build text for expense DTO, because Paid by user doesn't exist"))
                        .getName()
        ));
        sb.append(String.format("🗓️ *Date:* %s\n\n", expenseUpdateDto.getDate().format(formatter)));

        sb.append("👥 *Shared with:*\n");
        for (Map.Entry<Long, BigDecimal> share : expenseUpdateDto.getSharedUsers().entrySet()) {
            sb.append(String.format("  - %s: `%.2f`\n",
                    userRepository.findUserByTelegramId(
                            share.getKey()
                    ).orElseThrow(() -> new UserNotFoundException("Can't build text for expense DTO, because Paid by user doesn't exist"))
                            .getName(),
                    share.getValue()));
        }
        log.info("Returning text for expense DTO");
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Expense> getExpenseById(Long expenseId) {
        return expenseRepository.findById(expenseId);
    }

    @Override
    public void deleteExpenseById(Long expenseId) {
        expenseRepository.delete(expenseRepository.findExpenseById(expenseId)
                .orElseThrow(
                        () -> new ExpenseNotFoundException("Expense with id " + expenseId + " can't be deleted, because it doesn't exist")));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseUpdateDto buildExpenseUpdateDto(Long chatId, Long expenseId) {
        Expense expense = expenseRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Can't set fields to DTO, because source expense doesn't exist"));
        List<ExpenseShare> expenseShares = expenseShareRepository.findByExpenseId(expenseId);
        return userStateManager.getOrCreateExpenseUpdateDto(chatId).fromEntity(expense, expenseShares);
    }

    @Override
    @Transactional
    public Long saveExpenseUpdateDto(Long chatId) {
        ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);

        Expense expense = expenseRepository.findExpenseById(expenseUpdateDto.getId())
                .orElseThrow(() -> new ExpenseNotFoundException("Can't save expense dto, because target expense doesn't exist"));

        // update fields
        expense.setTitle(expenseUpdateDto.getTitle());
        expense.setAmount(expenseUpdateDto.getAmount());
        expense.setDate(expenseUpdateDto.getDate());
        expense.setPaidBy(
                userRepository.findUserByTelegramId(expenseUpdateDto.getPaidByUserId())
                        .orElseThrow(() -> new UserNotFoundException("Can't set user, because it doesn't exist"))
        );
        expense.setCreatedAt(expenseUpdateDto.getDate());

        // rebuild shares
        expenseShareRepository.deleteByExpenseId(expenseUpdateDto.getId());
        expenseShareRepository.flush(); // not sure if it is safe to use this method

        List<ExpenseShare> shares = expenseUpdateDto.getSharedUsers().entrySet().stream()
                .map(entry -> {
                    Long telegramId = entry.getKey();
                    BigDecimal amount = entry.getValue();

                    User user = userRepository.findUserByTelegramId(telegramId)
                            .orElseThrow(() -> new UserNotFoundException("Can't create share, user doesn't exist: " + telegramId));

                    ExpenseShare share = new ExpenseShare();
                    share.setExpense(expense);
                    share.setUser(user);
                    share.setAmount(amount);
                    return share;
                })
                .toList();

        expenseShareRepository.saveAll(shares);
        return expense.getId();
    }



    @Override
    public String getBalanceText(String groupId) {
        Map<User, BigDecimal> balance = self.calculateBalance(groupId);

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
