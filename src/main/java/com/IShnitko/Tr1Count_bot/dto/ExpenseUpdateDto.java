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
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ExpenseUpdateDto {
    private Long id;
    private String title;
    private BigDecimal amount;
    private Long paidByUserId;
    private LocalDateTime date;
    private Map<Long, BigDecimal> sharedUsers = new HashMap<>();

    public static ExpenseUpdateDto fromEntity(Expense expense, List<ExpenseShare> expenseShares) {
        ExpenseUpdateDto dto = new ExpenseUpdateDto();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setPaidByUserId(expense.getPaidBy().getTelegramId());
        dto.setDate(expense.getDate());

        Map<Long, BigDecimal> map = new HashMap<>();
        for (ExpenseShare share : expenseShares) {
            map.put(share.getUser().getTelegramId(), share.getAmount());
        }
        dto.setSharedUsers(map);

        return dto;
    }

    public void applyToEntity(Expense expense, List<ExpenseShare> expenseShares, Map<Long, User> userCache) {
        if (title != null) {
            expense.setTitle(title);
        }
        if (amount != null) {
            expense.setAmount(amount);
        }
        if (paidByUserId != null && userCache.containsKey(paidByUserId)) {
            expense.setPaidBy(userCache.get(paidByUserId));
        }
        if (date != null) {
            expense.setDate(date);
        }

        if (sharedUsers != null && !sharedUsers.isEmpty()) {
            for (ExpenseShare share : expenseShares) {
                BigDecimal newAmount = sharedUsers.get(share.getUser().getTelegramId());
                if (newAmount != null) {
                    share.setAmount(newAmount);
                }
            }
        }
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
