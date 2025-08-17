package com.IShnitko.Tr1Count_bot.dto;

import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private Integer messageId; // TODO: use this more and change deleting messages to editing

    public void initializeSharedUsers(List<User> users) {
        if (users != null) {
            users.forEach(user -> sharedUsers.put(user.getTelegramId(), true));
        }
    }
}
