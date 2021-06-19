package io.github.revxrsal.eventbus;

import org.jetbrains.annotations.NotNull;

/**
 * An exception handler responsible for catching and dealing with exceptions
 * thrown from event subscriptions upon invocation.
 */
public interface EventExceptionHandler {

    /**
     * Handles the exception
     *
     * @param subscription The subscription that failed
     * @param event        The dispatched event
     * @param throwable    The throwable
     */
    void handleException(@NotNull Subscription subscription, @NotNull Object event, @NotNull Throwable throwable);

    /**
     * A simple {@link EventExceptionHandler} that prints stack trace upon fail.
     */
    EventExceptionHandler PRINT_STACKTRACE = (subscription, event, throwable) -> throwable.printStackTrace();

}
