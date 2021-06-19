package io.github.revxrsal.eventbus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for collecting information about an event subscription
 */
@SuppressWarnings("rawtypes")
public final class Subscription {

    private final EventListener listener; // keep ungenerified
    private final Object instance;
    private final String name;
    private final Class<?> eventType;

    public Subscription(EventListener listener, Object instance, String name, Class<?> eventType) {
        this.listener = listener;
        this.instance = instance;
        this.name = name;
        this.eventType = eventType;
    }

    /**
     * Tests whether should this subscription be invoked for the
     * given event type
     *
     * @param hierarchical Whether should the subscription accept the event if
     *                     it is a subclass of the subscription's event type.
     * @param eventType    Event type to test for
     * @return True if this should be invoked, false if otherwise.
     */
    public boolean shouldInvoke(boolean hierarchical, @NotNull Class<?> eventType) {
        return hierarchical ? this.eventType.isAssignableFrom(eventType) : this.eventType == eventType;
    }

    /**
     * Returns the event listener of this subscription
     *
     * @return The event listener
     */
    public EventListener getListener() {
        return listener;
    }

    /**
     * Returns the instance of this subscription. This will be null in cases
     * of {@link EventBus#registerListener(Class, EventListener)}.
     *
     * @return The listener instance.
     */
    public @Nullable Object getInstance() {
        return instance;
    }

    /**
     * Returns the name of the subscription. This will vary depending
     * on the subscription source.
     * <p>
     * For example, in cases of methods, this will represent the method's
     * class and name.
     *
     * @return The subscription name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the event type that this subscription listens to
     *
     * @return The event type
     */
    public Class<?> getEventType() {
        return eventType;
    }

    @Override public String toString() {
        return "Subscription(" + name + ")";
    }
}
