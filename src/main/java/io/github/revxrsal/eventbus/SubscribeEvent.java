package io.github.revxrsal.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark methods that handle events, or fields of
 * type {@link EventListener}.
 *
 * Note that it is possible to specify additional annotations
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeEvent {
}
