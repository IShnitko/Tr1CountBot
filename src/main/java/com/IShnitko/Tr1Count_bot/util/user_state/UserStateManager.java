package com.IShnitko.Tr1Count_bot.util.user_state;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateManager {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, null);
    }

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }

    public void clearUserData(Long chatId) {
        clearState(chatId);
    }

}
