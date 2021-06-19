package io.github.revxrsal.eventbus.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the index of a generated parameter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {

    /**
     * Returns the index of the parameter.
     *
     * @return The index of the parameter.
     */
    int value();

}
