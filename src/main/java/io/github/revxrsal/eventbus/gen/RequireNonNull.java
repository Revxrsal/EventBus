package io.github.revxrsal.eventbus.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark parameters in setter methods inside generated event classes
 * as non-null
 * <p>
 * This will insert an additional {@link java.util.Objects#requireNonNull(Object, String)} before
 * setting the field value.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireNonNull {

    /**
     * The error message. <l>$field</l> will be replaced by the field name.
     *
     * @return The error message
     */
    String value() default "$field cannot be null!";

}
