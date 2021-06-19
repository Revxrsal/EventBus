package io.github.revxrsal.eventbus;

import io.github.revxrsal.eventbus.base.SimpleEventBusBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;

/**
 * Builder class for {@link EventBus}, with more customizabitily for
 * annotations, executors, etc.
 */
public interface EventBusBuilder {

    /**
     * Sets the {@link Executor} for executing event listeners.
     *
     * @param executor Executor to set
     * @return This builder instance
     */
    @NotNull EventBusBuilder executor(@NotNull Executor executor);

    /**
     * Sets the exception handler for the event bus
     *
     * @param exceptionHandler The exception handler
     * @return This builder instance
     * @see EventExceptionHandler
     */
    @NotNull EventBusBuilder exceptionHandler(@Nullable EventExceptionHandler exceptionHandler);

    /**
     * Adds extra annotations to scan subscribed methods and fields for.
     *
     * @param annotations Additional annotations
     * @return This builder instance
     */
    @NotNull EventBusBuilder scanAnnotations(@NotNull Class<? extends Annotation>... annotations);

    /**
     * Disables hierarchical invocation. This means that if a subscription listens
     * to an event and a subclass of that event is dispatched, it will not
     * be invoked.
     *
     * @return This builder instance
     */
    @NotNull EventBusBuilder disableHierarchicalInvocation();

    /**
     * Constructs an immutable {@link EventBus} instance from this builder
     *
     * @return The event bus
     */
    @NotNull EventBus build();

    /**
     * Creates a new {@link EventBusBuilder} that uses the ASM library
     * to invoke subscriptions.
     * <p>
     * This is better than othes event buses as it performs as fast
     * as direct method invocation, as no reflections is needed.
     *
     * @return The new event bus builder
     * @see EventBusBuilder#methodHandles()
     * @see EventBusBuilder#reflection()
     */
    static @NotNull EventBusBuilder asm() {
        return SimpleEventBusBuilder.asm();
    }

    /**
     * Creates a new {@link EventBusBuilder} that uses the new Java-7-introduced
     * {@link java.lang.invoke.MethodHandles} API to invoke its subscriptions. This
     * is supposedly faster than standard Java reflections.
     *
     * @return The new event bus builder
     * @see EventBusBuilder#asm()
     * @see EventBusBuilder#reflection()
     */
    static @NotNull EventBusBuilder methodHandles() {
        return SimpleEventBusBuilder.methodHandles();
    }

    /**
     * Creates a new {@link EventBusBuilder} that uses the traditional reflection API
     * to invoke its subscriptions.
     *
     * @return The new event bus builder
     * @see EventBusBuilder#asm()
     * @see EventBusBuilder#methodHandles()
     */
    static @NotNull EventBusBuilder reflection() {
        return SimpleEventBusBuilder.reflection();
    }

}
