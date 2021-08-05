package io.github.revxrsal.eventbus.base;

import io.github.revxrsal.eventbus.EventBus;
import io.github.revxrsal.eventbus.EventBusBuilder;
import io.github.revxrsal.eventbus.EventExceptionHandler;
import io.github.revxrsal.eventbus.SubscribeEvent;
import io.github.revxrsal.eventbus.asm.ASMEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class SimpleEventBusBuilder implements EventBusBuilder {

    private EventExceptionHandler exceptionHandler;
    private Executor executor = DEFAULT_EXECUTOR;
    private boolean hierarchicalInvocation = true;
    private final List<Class<? extends Annotation>> annotations = new ArrayList<>();
    private final String type;

    private SimpleEventBusBuilder(String type) {
        this.type = type;
        annotations.add(SubscribeEvent.class);
    }

    public @NotNull SimpleEventBusBuilder executor(@NotNull Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
        return this;
    }

    public @NotNull SimpleEventBusBuilder exceptionHandler(@Nullable EventExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @SafeVarargs @Override public @NotNull final EventBusBuilder scanAnnotations(@NotNull Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> c : annotations) {
            Retention retention = c.getAnnotation(Retention.class);
            RetentionPolicy policy = retention == null ? RetentionPolicy.CLASS : retention.value();
            if (policy != RetentionPolicy.RUNTIME) {
                throw new IllegalArgumentException("Annotation " + c.getName() + " has retention of RetentionPolicy." + policy + ". It must use RetentionPolicy.RUNTIME to become visible for scanning.");
            }
            this.annotations.add(c);
        }
        return this;
    }

    @Override public @NotNull EventBusBuilder disableHierarchicalInvocation() {
        hierarchicalInvocation = false;
        return this;
    }

    public static SimpleEventBusBuilder asm() {
        return new SimpleEventBusBuilder(ASM);
    }

    public static SimpleEventBusBuilder methodHandles() {
        return new SimpleEventBusBuilder(MH);
    }

    public static SimpleEventBusBuilder reflection() {
        return new SimpleEventBusBuilder(REFLECTION);
    }

    public @NotNull EventBus build() {
        if (exceptionHandler == null) exceptionHandler = EventExceptionHandler.PRINT_STACKTRACE;
        switch (type) {
            case MH:
                return new MethodHandlesEventBus(exceptionHandler, executor, annotations, hierarchicalInvocation);
            case REFLECTION:
                return new ReflectionEventBus(exceptionHandler, executor, annotations, hierarchicalInvocation);
            default: {
                return new ASMEventBus(exceptionHandler, executor, annotations, hierarchicalInvocation);
            }
        }
    }

    static final Executor DEFAULT_EXECUTOR = Runnable::run;
    private static final String ASM = "io.github.revxrsal.eventbus.asm.ASMEventBus";
    private static final String MH = "io.github.revxrsal.eventbus.base.MethodHandlesEventBus";
    private static final String REFLECTION = "io.github.revxrsal.eventbus.base.ReflectionEventBus";

}
