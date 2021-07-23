package io.github.revxrsal.eventbus;

import io.github.revxrsal.eventbus.gen.Index;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Represents an event bus responsible for dispatching events to subscribed
 * listeners.
 * <p>
 * This class is thread-safe and can be used in multi-threaded
 * environments.
 * <p>
 * Create instances with {@link EventBusBuilder}.
 *
 * @see EventBusBuilder
 * @see PostResult
 * @see SubscribeEvent
 * @see Subscription
 */
public interface EventBus {

    /**
     * Publishes an event to all listeners. This is the same as calling
     * {@link #post(Object)} but returns a {@link PostResult} containing
     * the execution information. Useful for debugging.
     *
     * @param event Event to dispatch
     * @param <T>   The event type
     * @return The event result, wrapped in a future in cases of asynchronous
     * execution.
     */
    <T> CompletableFuture<PostResult<T>> publish(@NotNull T event);

    /**
     * Publishes an generated event to all listeners. This is the same as calling
     * {@link #post(Class)} but returns a {@link PostResult} containing
     * the execution information. Useful for debugging.
     * <p>
     * Note that this will throw an {@link UnsupportedOperationException} if
     * this event bus was not constructed with {@link EventBusBuilder#asm()}.
     *
     * @param event Event class to dispatch. Must be an interface
     * @param <T>   The event type
     * @return The event result, wrapped in a future in cases of asynchronous
     * execution.
     */
    <T> CompletableFuture<PostResult<T>> publish(@NotNull Class<T> event);

    /**
     * Publishes an generated event to all listeners. This is the same as calling
     * {@link #post(Class)} but returns a {@link PostResult} containing
     * the execution information. Useful for debugging.
     * <p>
     * Note that this will throw an {@link UnsupportedOperationException} if
     * this event bus was not constructed with {@link EventBusBuilder#asm()}.
     *
     * @param event      Event class to dispatch. Must be an interface
     * @param parameters Event parameters in the correct order of their
     *                   their {@link Index}
     * @param <T>        The event type
     * @return The event result.
     */
    <T> CompletableFuture<PostResult<T>> publish(@NotNull Class<T> event, Object... parameters);

    /**
     * Publishes this event to all listeners.
     *
     * @param event Event to publish
     * @return A future indicating the progress of posting the event
     * to listeners.
     * @see #publish(Object)
     */
    CompletableFuture<Void> post(@NotNull Object event);

    /**
     * Publishes the generated event to all listeners.
     *
     * @param event Event class to generate
     * @return A future indicating the progress of posting the event
     * to listeners.
     * @see #publish(Class)
     */
    CompletableFuture<Void> post(@NotNull Class<?> event);

    /**
     * Publishes the generated event to all listeners.
     *
     * @param event      Event class to generate
     * @param parameters Event parameters in the appropriate order as
     *                   defined by {@link Index}
     * @return A future indicating the progress of posting the event
     * to listeners.
     * @see #publish(Class, Object[])
     */
    CompletableFuture<Void> post(@NotNull Class<?> event, Object... parameters);

    /**
     * Publishes this event to all listeners, and returns the event instantly.
     *
     * <strong>Note that this will not respect futures if the event bus is asynchronous!</strong>
     *
     * @param event Event to publish
     * @return The posted event
     * @see #publish(Object)
     */
    <T> T submit(@NotNull T event);

    /**
     * Publishes this event to all listeners, and returns the event instantly.
     *
     * <strong>Note that this will not respect futures if the event bus is asynchronous!</strong>
     *
     * @param eventType Event class to generate
     * @return The posted event
     * @see #publish(Class)
     */
    <T> T submit(@NotNull Class<T> eventType);

    /**
     * Publishes the generated event to all listeners.
     *
     * @param eventType      Event class to generate
     * @param parameters Event parameters in the appropriate order as
     *                   defined by {@link Index}
     * @return The posted event
     * @see #publish(Class, Object[])
     */
    <T> T submit(@NotNull Class<T> eventType, Object... parameters);

    /**
     * A utility method to automatically pre-generate all the required
     * stuff for invoking the event.
     * <p>
     * Note that this will throw an {@link UnsupportedOperationException} if
     * this event bus was not constructed with {@link EventBusBuilder#asm()}.
     *
     * @param event Event to generate for.
     */
    void preGenerate(@NotNull Class<?>... event);

    /**
     * Registers the specified instance by scanning for listeners.
     * <p>
     * Registered elements:
     * <ol>
     *     <li>Methods annotated with {@link SubscribeEvent}
     *     or any annotation marked by {@link EventBusBuilder#scanAnnotations(Class[])}.</li>
     *     <li>Fields of type {@link EventListener} and are annotated with {@link SubscribeEvent}
     *     or any annotations marked by {@link EventBusBuilder#scanAnnotations(Class[])}</li>
     * </ol>
     * <p>
     * Example:
     *
     * <pre>
     *     eventBus.register(new MyListener());
     *     // or
     *     eventBus.register(MyListener.class); // if all listener fields and methods are static.
     * </pre>
     *
     * @param listener Object to scan. Can be a {@link Class} if the elements
     *                 are static.
     * @see #registerListener(Class, EventListener)
     * @see #unregister(Object)
     */
    void register(@NotNull Object listener);

    /**
     * Registers the specified listener for the given event type
     *
     * @param eventType Event class to register for
     * @param listener  Listener to register
     * @param <T>       The event type
     */
    <T> void registerListener(@NotNull Class<T> eventType, @NotNull EventListener<T> listener);

    /**
     * Registers the specified {@link EventListener}. This will try to evaluate
     * the event type, but may fail in certain cases.
     * <p>
     * For ungenerified types, this will use event type <code>Object.class</code>
     * <p>
     * For more precise evaluation, use {@link #registerListener(Class, EventListener)}.
     *
     * @param listener Listener to register
     * @param <T>      The event type
     */
    <T> void registerListener(@NotNull EventListener<T> listener);

    /**
     * Unregisters the specified listener instance, by unsubscribing all
     * fields and methods.
     *
     * @param listener Listener instance to remove
     */
    void unregister(@NotNull Object listener);

    /**
     * Unregisters the specified listener.
     *
     * @param listener Listener to unregister
     * @param <T>      The event type
     */
    <T> void unregister(@NotNull EventListener<T> listener);

    /**
     * Returns the executor used by this event bus.
     *
     * @return The event executor
     */
    Executor getExecutor();

    /**
     * Returns the exception handler responsible for dealing with
     * exceptions thrown from listeners.
     *
     * @return The exception handler
     */
    EventExceptionHandler getEventExceptionHandler();

}
