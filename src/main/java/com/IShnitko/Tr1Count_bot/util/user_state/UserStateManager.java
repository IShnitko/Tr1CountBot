package com.IShnitko.Tr1Count_bot.util.user_state;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateManager {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> chosenGroups = new ConcurrentHashMap<>();

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

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }

    public void clearUserData(Long chatId) {
        clearState(chatId);
        chosenGroups.remove(chatId);
    }

}
