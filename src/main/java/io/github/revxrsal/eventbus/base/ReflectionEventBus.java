package io.github.revxrsal.eventbus.base;

import io.github.revxrsal.eventbus.EventExceptionHandler;
import io.github.revxrsal.eventbus.EventListener;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

class ReflectionEventBus extends BaseEventBus {

    public ReflectionEventBus(EventExceptionHandler exceptionHandler, Executor executor, List<Class<? extends Annotation>> annotations, boolean hierarchicalInvocation) {
        super(exceptionHandler, executor, annotations, hierarchicalInvocation);
    }

    @Override protected <T> EventListener<T> createEventListener(@NotNull Object listenerInstnace, @NotNull Method method) {
        try {
            if (!method.isAccessible())
                method.setAccessible(true);
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot make method " + method.getName() + " in " + method.getDeclaringClass() + " accessible reflectively. Maybe make it public?");
        }
        return event -> method.invoke(listenerInstnace, event);
    }
}
