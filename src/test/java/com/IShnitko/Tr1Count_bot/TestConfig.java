package com.IShnitko.Tr1Count_bot;

import com.IShnitko.Tr1Count_bot.bot.Tr1CountBot;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public TelegramBotsApi testTelegramBotsApi() throws TelegramApiException {
        TelegramBotsApi mockApi = Mockito.mock(TelegramBotsApi.class);
        // Настройте поведение мока при необходимости
        return mockApi;
    }
}