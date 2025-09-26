package com.IShnitko.Tr1Count_bot.dto;

import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.ExpenseShare;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ExpenseUpdateDto implements SharedUsersProvider {
    private Long id;
    private String title;
    private BigDecimal amount;
    private Long paidByUserId;
    private LocalDateTime date;
    private Map<Long, BigDecimal> sharedUsers = new HashMap<>();

    public ExpenseUpdateDto fromEntity(Expense expense, List<ExpenseShare> expenseShares) {
        this.id = expense.getId();
        this.title = expense.getTitle();
        this.amount = expense.getAmount();
        this.paidByUserId = expense.getPaidBy().getTelegramId();
        this.date = expense.getDate();

        Map<Long, BigDecimal> map = new HashMap<>();
        for (ExpenseShare share : expenseShares) {
            map.put(share.getUser().getTelegramId(), share.getAmount());
        }
        this.sharedUsers = map;

        return this;
    }

    @Override
    public void initializeSharedUsers(List<User> users) {
        if (users != null) {
            users.forEach(user -> sharedUsers.putIfAbsent(user.getTelegramId(), BigDecimal.ZERO));
        }
    }

    @Override
    public boolean isUserShared(Long telegramId) {
        return sharedUsers.containsKey(telegramId);
    }

    @Override
    public String getUserLabel(Long telegramId, UserService userService) {
        BigDecimal amount = sharedUsers.get(telegramId);
        return amount != null ? String.format("💵 %.2f", amount) : "❌";
    }

    public String toString(UserService userService) {
        StringBuilder sb = new StringBuilder();
        sb.append("✏️ *Updated Expense Preview* ✏️\n\n");

        sb.append("📝 *Title*: ").append(title != null ? title : "—").append("\n");
        sb.append("💵 *Amount*: ").append(amount != null ? amount : "—").append("\n");

        String paidByUserName = paidByUserId != null
                ? userService.getUserNameById(paidByUserId)
                : "—";
        sb.append("👤 *Paid by*: ").append(paidByUserName).append("\n");

        sb.append("\n🗓️ *Date*: ");
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            sb.append(date.format(formatter));
        } else {
            sb.append("—");
        }
        sb.append("\n\n");

        sb.append("👥 *Shared with:*\n");

        if (sharedUsers == null || sharedUsers.isEmpty()) {
            throw new IllegalArgumentException("Shared users cannot be empty");
        } else {
            for (Map.Entry<Long, BigDecimal> entry : sharedUsers.entrySet()) {
                String name = userService.getUserNameById(entry.getKey());
                sb.append(String.format("  - %s: `%.2f`\n", name, entry.getValue()));
            }
        }

        return sb.toString();
    }
}
