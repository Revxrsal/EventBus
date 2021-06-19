package io.github.revxrsal.eventbus.asm;

import io.github.revxrsal.eventbus.EventExceptionHandler;
import io.github.revxrsal.eventbus.EventListener;
import io.github.revxrsal.eventbus.PostResult;
import io.github.revxrsal.eventbus.base.BaseEventBus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ASMEventBus extends BaseEventBus {

    public ASMEventBus(EventExceptionHandler exceptionHandler, Executor executor, List<Class<? extends Annotation>> annotations, boolean hierarchicalInvocation) {
        super(exceptionHandler, executor, annotations, hierarchicalInvocation);
    }

    @Override protected <T> EventListener<T> createEventListener(@NotNull Object listenerInstnace, @NotNull Method method) {
        return ASMEventListenerGen.generateListener(listenerInstnace, method);
    }

    @Override public <T> CompletableFuture<PostResult<T>> publish(@NotNull Class<T> eventType, Object... parameters) {
        T event = EventGenerator.generate(eventType, parameters);
        return super.publish(event);
    }

    @Override public <T> CompletableFuture<PostResult<T>> publish(@NotNull Class<T> eventType) {
        T event = EventGenerator.generate(eventType);
        return super.publish(event);
    }

    @Override public CompletableFuture<Void> post(@NotNull Class<?> eventType) {
        Object event = EventGenerator.generate(eventType);
        return super.post(event);
    }

    @Override public CompletableFuture<Void> post(@NotNull Class<?> eventType, Object... parameters) {
        Object event = EventGenerator.generate(eventType, parameters);
        return super.post(event);
    }

    @Override public void preGenerate(@NotNull Class<?> event) {
        EventGenerator.generateFactory(event);
    }
}
