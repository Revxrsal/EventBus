package io.github.revxrsal.eventbus.asm;

import java.lang.reflect.Array;
import java.util.StringJoiner;

public class GeneratedToStringBuilder {

    private final StringJoiner joiner;

    public GeneratedToStringBuilder(String name) {
        joiner = new StringJoiner(", ", name + "{", "}");
    }

    public void append(String name, Object value) {
        if (value == null) {
            joiner.add(name + "=null");
        } else {
            if (value.getClass().isArray()) {
                joiner.add(name + "=" + toString(value));
            } else {
                joiner.add(name + "=" + value);
            }
        }
    }

    @Override public String toString() {
        return joiner.toString();
    }

    private static String toString(Object a) {
        if (a == null)
            return "null";

        int iMax = Array.getLength(a) - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(Array.get(a, i));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

}
