package com.IShnitko.Tr1Count_bot.bot.user_state;

import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateManager {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> chosenGroups = new ConcurrentHashMap<>();
    private final Map<Long, Integer> messageIds = new ConcurrentHashMap<>();
    private final Map<Long, CreateExpenseDto> userCreateExpenseDtos = new ConcurrentHashMap<>();
    private final Map<Long, ExpenseUpdateDto> userExpenseUpdateDtos = new ConcurrentHashMap<>();
    private final Map<Long, Integer> expenseMenuPage = new ConcurrentHashMap<>();

    // States

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public void setStateWithChosenGroup(Long chatId, UserState state, String groupId) {
        userStates.put(chatId, state);
        chosenGroups.put(chatId, groupId);
    }

    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.DEFAULT);
    }

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }

    public void clearUserData(Long chatId) {
        clearState(chatId);
        chosenGroups.remove(chatId);
        userCreateExpenseDtos.remove(chatId);
    }

    // Groups

    public String getChosenGroup(Long chatId) {
        return chosenGroups.get(chatId);
    }

    public void clearChosenGroup(Long chatId) {
        chosenGroups.remove(chatId);
    }

    // Saved message id

    public void setBotMessageId(Long chatId, Integer messageId) {
        messageIds.put(chatId, messageId);
    }

    public Integer getBotMessageId(Long chatId) {
        return messageIds.getOrDefault(chatId, null);
    }

    // Crete Expense DTO

    public CreateExpenseDto getOrCreateExpenseDto(Long chatId) {
        return userCreateExpenseDtos.computeIfAbsent(chatId, k -> new CreateExpenseDto());
    }

    public void clearExpenseDto(Long chatId) {
        userCreateExpenseDtos.remove(chatId);
    }

    // Saved expense menu page

    public Integer getAndIncPage(Long chatId) {
        Integer page = expenseMenuPage.get(chatId);
        if (page == null) {
            expenseMenuPage.put(chatId, 1);
            return 1;
        } else {
            expenseMenuPage.put(chatId,
                    page + 1);
            return page + 1;
        }
    }

    public Integer getAndDecPage(Long chatId) {
        Integer prevPage = expenseMenuPage.get(chatId);
        expenseMenuPage.put(chatId,
                prevPage - 1);
        return prevPage - 1;
    }

    // Update Expense DTO

    public ExpenseUpdateDto getOrCreateExpenseUpdateDto(Long chatId) {
        return userExpenseUpdateDtos.computeIfAbsent(chatId, k -> new ExpenseUpdateDto());
    }

    public void clearExpenseUpdateDto(Long chatId) {
        userExpenseUpdateDtos.remove(chatId);
    }

}
