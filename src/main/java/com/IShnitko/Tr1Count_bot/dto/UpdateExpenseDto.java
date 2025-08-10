package com.IShnitko.Tr1Count_bot.dto;

import com.IShnitko.Tr1Count_bot.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record UpdateExpenseDto(
        Optional<List<User>> newSharedUsers,
        Optional<Long> paidByUserId,
        Optional<String> title,
        Optional<BigDecimal> amount,
        Optional<LocalDateTime> date
) {}