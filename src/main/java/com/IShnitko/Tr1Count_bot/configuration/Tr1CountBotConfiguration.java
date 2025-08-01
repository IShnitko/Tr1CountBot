package com.IShnitko.Tr1Count_bot.configuration;

import com.IShnitko.Tr1Count_bot.bot.Tr1CountBot;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class Tr1CountBotConfiguration {

    @Bean
    @Profile("!test")
    public TelegramBotsApi telegramBotsApi(Tr1CountBot tr1CountBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(tr1CountBot);
        return api;
    }

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }

}
