package com.IShnitko.Tr1Count_bot.dto;

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
public class CreateExpenseDto {
    private String title;
    private BigDecimal amount;
    private Long paidByUserId;
    private Map<Long, Boolean> sharedUsers = new HashMap<>();
    private LocalDateTime date;
    private Integer messageId;

    public void initializeSharedUsers(List<User> users) {
        if (users != null) {
            users.forEach(user -> sharedUsers.put(user.getTelegramId(), true));
        }
    }

    public String toString(UserService userService) {
        StringBuilder builder = new StringBuilder();
        builder.append("💸 *New Expense*:\n\n");
        builder.append("💵 *Title*: ").append(title).append("\n");
        builder.append("💰 *Amount*: ").append(amount).append("\n");

        String paidByUserName = userService.getUserNameById(paidByUserId);
        builder.append("👤 *Paid by*: ").append(paidByUserName).append("\n\n");

        builder.append("👥 *Shared with*:\n");
        List<String> sharedUserNames = sharedUsers.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> userService.getUserNameById(entry.getKey()))
                .collect(Collectors.toList());

        if (sharedUserNames.isEmpty()) {
            builder.append("- No one\n");
        } else {
            builder.append(String.join(", ", sharedUserNames)).append("\n");
        }

        builder.append("\n🗓️ *Date*: ");
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            builder.append(date.format(formatter));
        } else {
            builder.append("Not specified");
        }

        return builder.toString();
    }
}
