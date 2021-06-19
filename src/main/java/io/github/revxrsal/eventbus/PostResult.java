package io.github.revxrsal.eventbus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * Represents the result returned by {@link EventBus#publish(Object)} methods that
 * contains information about the event execution. This may be helpful
 * for debugging.
 *
 * @param <T> The event type
 */
public interface PostResult<T> {

    /**
     * Returns the invoked event
     *
     * @return The invoked event
     */
    @NotNull T getEvent();

    /**
     * Returns the successful invocations, in which no exception was thrown
     * when called.
     *
     * @return The successful calls
     */
    int getSuccessfulCalls();

    /**
     * Returns the failed invocations, in which an exception was thrown
     * when called.
     *
     * @return The failed calls
     */
    int getFailedCalls();

    /**
     * Returns the execution times for each listener in milliseconds.
     *
     * @return The execution times for listeners, in milliseconds.
     */
    @NotNull @Unmodifiable Map<Subscription, Long> getExecutionTimes();

}