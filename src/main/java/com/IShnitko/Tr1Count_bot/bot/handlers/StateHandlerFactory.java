package com.IShnitko.Tr1Count_bot.bot.handlers;

import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class StateHandlerFactory {
    private final Map<UserState, StateHandler> handlers;
    private final StateHandler defaultHandler;

    @Autowired
    public StateHandlerFactory(
            List<StateHandler> stateHandlers,
            DefaultStateHandler defaultHandler
    ) {
        handlers = new EnumMap<>(UserState.class);
        this.defaultHandler = defaultHandler;

        for (StateHandler handler : stateHandlers) {
            if (handler.getClass().isAnnotationPresent(StateHandlerFor.class)) {
                StateHandlerFor annotation = handler.getClass()
                        .getAnnotation(StateHandlerFor.class);
                handlers.put(annotation.value(), handler);
            }
        }
    }

    public StateHandler getHandler(UserState state) {
        return handlers.getOrDefault(state, defaultHandler);
    }
}