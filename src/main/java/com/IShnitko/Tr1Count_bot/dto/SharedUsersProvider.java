package com.IShnitko.Tr1Count_bot.dto;

import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.UserService;

import java.util.List;

public interface SharedUsersProvider {
    void initializeSharedUsers(List<User> users);
    boolean isUserShared(Long telegramId);
    String getUserLabel(Long telegramId, UserService userService);
}
