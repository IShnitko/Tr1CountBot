package com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation;

import com.IShnitko.Tr1Count_bot.model.UserState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StateHandlerFor {
    UserState value();
}