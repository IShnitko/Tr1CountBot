package com.IShnitko.Tr1Count_bot.bot.user_state;

import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateManager {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> chosenGroups = new ConcurrentHashMap<>();
    private final Map<Long, Integer> messageIds = new ConcurrentHashMap<>();
    private final Map<Long, CreateExpenseDto> userExpenseDtos = new ConcurrentHashMap<>();

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public void setStateWithChosenGroup(Long chatId, UserState state, String groupId){
        userStates.put(chatId, state);
        chosenGroups.put(chatId, groupId);
    }

    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.DEFAULT);
    }

    public String getChosenGroup(Long chatId) {
        return chosenGroups.get(chatId);
    }

    public void setBotMessageId(Long chatId, Integer messageId) {
        messageIds.put(chatId, messageId);
    }

    public Integer getBotMessageId(Long chatId) {
        return messageIds.getOrDefault(chatId, null);
    }

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }

    public void clearUserData(Long chatId) {
        clearState(chatId);
        chosenGroups.remove(chatId);
        userExpenseDtos.remove(chatId);
    }

    public void clearChosenGroup(Long chatId) {
        chosenGroups.remove(chatId);
    }

    public CreateExpenseDto getOrCreateExpenseDto(Long chatId) {
        return userExpenseDtos.computeIfAbsent(chatId, k -> new CreateExpenseDto());
    }

    public void clearExpenseDto(Long chatId) {
        userExpenseDtos.remove(chatId);
    }
}
