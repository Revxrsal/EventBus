package io.github.revxrsal.eventbus.asm;

import io.github.revxrsal.eventbus.EventListener;
import org.jetbrains.annotations.NotNull;

public interface ASMEventExecutor {

    void invokeASMEvent(Object listener, Object event) throws Throwable;

    default <T> EventListener<T> bindTo(@NotNull Object instance) {
        return event -> invokeASMEvent(instance, event);
    }

}
