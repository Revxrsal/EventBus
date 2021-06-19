package io.github.revxrsal.eventbus.base;

import io.github.revxrsal.eventbus.PostResult;
import io.github.revxrsal.eventbus.Subscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

class BasicMeasuredPostResult<T> implements PostResult<T> {

    private final T event;
    private final int success, fail;
    private final Map<Subscription, Long> executionTimes;

    public BasicMeasuredPostResult(T event, int success, int fail, Map<Subscription, Long> executionTimes) {
        this.event = event;
        this.success = success;
        this.fail = fail;
        this.executionTimes = executionTimes;
    }

    @Override public @NotNull T getEvent() {
        return event;
    }

    @Override public int getSuccessfulCalls() {
        return success;
    }

    @Override public int getFailedCalls() {
        return fail;
    }

    @Override public @NotNull @Unmodifiable Map<Subscription, Long> getExecutionTimes() {
        return executionTimes;
    }
}
