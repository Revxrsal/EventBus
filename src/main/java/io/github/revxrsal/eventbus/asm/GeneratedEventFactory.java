package io.github.revxrsal.eventbus.asm;

import org.jetbrains.annotations.NotNull;

public interface GeneratedEventFactory {

    @NotNull Object newEvent(Object... parameters);

}
