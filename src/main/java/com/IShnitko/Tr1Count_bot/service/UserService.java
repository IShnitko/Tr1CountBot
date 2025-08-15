package com.IShnitko.Tr1Count_bot.service;

import org.telegram.telegrambots.meta.api.objects.User;

public interface UserService {
    void findOrCreateUser(User telegramId);
    String getUserInfoForGroup(Long telegramId, String groupId);
}
