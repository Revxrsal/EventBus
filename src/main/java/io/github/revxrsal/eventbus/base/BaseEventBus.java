package io.github.revxrsal.eventbus.base;

import io.github.revxrsal.eventbus.EventListener;
import io.github.revxrsal.eventbus.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A base implementation of {@link EventBus}.
 */
public abstract class BaseEventBus implements EventBus {

    protected final Set<Subscription> subscriptions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    protected final EventExceptionHandler exceptionHandler;
    protected final Executor executor;
    protected final List<Class<? extends Annotation>> annotations;
    protected final boolean hierarchicalInvocation;

    public BaseEventBus(EventExceptionHandler exceptionHandler,
                        Executor executor,
                        List<Class<? extends Annotation>> annotations,
                        boolean hierarchicalInvocation) {
        this.exceptionHandler = exceptionHandler;
        this.executor = executor;
        this.annotations = annotations;
        this.hierarchicalInvocation = hierarchicalInvocation;
    }

    @Override public <T> CompletableFuture<PostResult<T>> publish(@NotNull T event) {
        return supplyAsync(() -> {
            Map<Subscription, Long> executionTimes = new HashMap<>();
            int failed = 0, success = 0;
            for (Subscription subscription : subscriptions) {
                if (subscription.shouldInvoke(hierarchicalInvocation, event.getClass())) {
                    try {
                        long time = System.currentTimeMillis();
                        subscription.getListener().handle(event);
                        executionTimes.put(subscription, System.currentTimeMillis() - time);
                        success++;
                    } catch (Throwable throwable) {
                        failed++;
                        exceptionHandler.handleException(subscription, event, throwable);
                    }
                }
            }
            return new BasicMeasuredPostResult<>(event, success, failed, Collections.unmodifiableMap(executionTimes));
        }, executor);
    }

    @Override public <T> CompletableFuture<PostResult<T>> publish(@NotNull Class<T> event) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public CompletableFuture<Void> post(@NotNull Class<?> event) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public <T> CompletableFuture<PostResult<T>> publish(@NotNull Class<T> event, Object... parameters) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public CompletableFuture<Void> post(@NotNull Class<?> event, Object... parameters) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public void preGenerate(@NotNull Class<?>... event) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public <T> T submit(@NotNull T event) {
        executor.execute(() -> {
            for (Subscription subscription : subscriptions) {
                if (subscription.shouldInvoke(hierarchicalInvocation, event.getClass())) {
                    try {
                        subscription.getListener().handle(event);
                    } catch (Throwable throwable) {
                        exceptionHandler.handleException(subscription, event, throwable);
                    }
                }
            }
        });
        return event;
    }

    @Override public <T> T submit(@NotNull Class<T> eventType) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public <T> T submit(@NotNull Class<T> eventType, Object... parameters) {
        throw new UnsupportedOperationException("Only EventBuses constructed from EventBusBuilder.asm() can use this method.");
    }

    @Override public CompletableFuture<Void> post(@NotNull Object event) {
        return runAsync(() -> {
            for (Subscription subscription : subscriptions) {
                if (subscription.shouldInvoke(hierarchicalInvocation, event.getClass())) {
                    try {
                        subscription.getListener().handle(event);
                    } catch (Throwable throwable) {
                        exceptionHandler.handleException(subscription, event, throwable);
                    }
                }
            }
        }, executor);
    }

    @Override public void register(@NotNull Object listenerInstance) {
        Class<?> cl = listenerInstance instanceof Class ? (Class<?>) listenerInstance : listenerInstance.getClass();
        for (Method method : cl.getDeclaredMethods()) {
            if (annotations.stream().noneMatch(method::isAnnotationPresent)) continue;
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Method " + method.getName() + " in " + cl
                        + " must only accept 1 parameter (Found: " + method.getParameterCount() + ")!");
            }

            if (listenerInstance == cl && !Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Method " + method.getName() + " in " + cl
                        + " is non-static but provided listener was not an instance!");
            }

            Class<?> eventType = method.getParameterTypes()[0];
            String name = String.format("%s.%s(%s)", cl.getName(), method.getName(), eventType.getSimpleName());
            EventListener<?> listener = createEventListener(listenerInstance, method);

            subscriptions.add(new Subscription(listener, listenerInstance, name, eventType));
        }
        for (Field field : cl.getDeclaredFields()) {
            if (annotations.stream().noneMatch(field::isAnnotationPresent)) continue;
            if (listenerInstance == cl && !Modifier.isStatic(field.getModifiers())) {
                throw new IllegalArgumentException("Field " + field.getName() + " in " + cl
                        + " is non-static but provided listener was not an instance!");
            }
            if (!EventListener.class.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException("Field " + field.getName() + " in " + cl
                        + "is not of type " + EventListener.class.getName() + ".");
            }
            try {
                Class<?> eventType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                String name = String.format("%s.%s(%s)", cl.getName(), field.getName(), eventType.getName());
                if (!field.isAccessible())
                    field.setAccessible(true);
                EventListener<?> listener = (EventListener<?>) field.get(listenerInstance);
                subscriptions.add(new Subscription(listener, listenerInstance, name, eventType));
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Could not evaluate event type from field " + field.getName() + " in "
                        + cl + ". Is it missing generics?");
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not reflectively get field " + field.getName() + " in " + cl + ". Maybe make it accessible?");
            }
        }
    }

    @Override public <T> void registerListener(@NotNull Class<T> eventType, @NotNull EventListener<T> listener) {
        subscriptions.add(new Subscription(listener, null, listener.getClass().getName(), eventType));
    }

    @SuppressWarnings("rawtypes")
    @Override public <T> void registerListener(@NotNull EventListener<T> listener) {
        try {
            for (Type type : listener.getClass().getGenericInterfaces()) {
                System.out.println(type);
                if (type.getTypeName().startsWith(EventListener.class.getName())) {
                    Class eventType = type instanceof ParameterizedType ? (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0] : Object.class;
                    registerListener(eventType, listener);
                }
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Failed to evaluate event type from listener " + listener.getClass().getName() + ". Use EventBus#registerListener(Class, EventListener)");
        }
    }

    @Override public void unregister(@NotNull Object listener) {
        subscriptions.removeIf(s -> Objects.equals(s.getInstance(), listener));
    }

    @Override public <T> void unregister(@NotNull EventListener<T> listener) {
        subscriptions.removeIf(s -> Objects.equals(s.getListener(), listener));
    }

    @Override public Executor getExecutor() {
        return executor;
    }

    @Override public EventExceptionHandler getEventExceptionHandler() {
        return exceptionHandler;
    }

    protected abstract <T> EventListener<T> createEventListener(@NotNull Object listenerInstnace, @NotNull Method method);

}
