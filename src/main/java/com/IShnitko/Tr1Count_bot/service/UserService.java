package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.model.User;

public interface UserService {
    User findOrCreateUser(org.telegram.telegrambots.meta.api.objects.User telegramId);

    String getUserInfoForGroup(Long telegramId, String groupId);

    String getUserNameById(Long telegramId);

    Long getCreatorOfTheGroup(String groupId);
}
