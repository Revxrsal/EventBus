package io.github.revxrsal.eventbus.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an additional bean property on the generated event class, which is mutable and
 * is not set using the constructor. The generated property will be
 * non-final, and can have getters and setters.
 *
 * Getters or setters must follow the getAbc / isAbc / setAbc convention, where 'abc' is the property name
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
}
