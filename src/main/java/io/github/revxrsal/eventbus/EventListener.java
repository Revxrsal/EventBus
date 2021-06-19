package io.github.revxrsal.eventbus;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a standard event listener that invokes code when an
 * event is dispached.
 *
 * @param <T> The event to listen for
 */
@FunctionalInterface
public interface EventListener<T> {

    /**
     * Handles the given event
     *
     * @param event Event to handle
     * @throws Throwable Any throwable during the invocation
     */
    void handle(@NotNull T event) throws Throwable;

}
