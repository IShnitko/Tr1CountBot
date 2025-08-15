package com.IShnitko.Tr1Count_bot.bot.handlers;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;

public interface StateHandler {
    void handle(ChatContext context) throws Exception;
}