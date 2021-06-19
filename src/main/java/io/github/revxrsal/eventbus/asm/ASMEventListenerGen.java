package io.github.revxrsal.eventbus.asm;

import io.github.revxrsal.eventbus.EventListener;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A delegating event listener that uses ASM to invoke the listener. This
 * should improve performance and match up to be as fast as normal method
 * invocation, without the need to use reflection.
 */
class ASMEventListenerGen implements Opcodes {

    private static final String[] INTERFACES = new String[]{Type.getInternalName(ASMEventExecutor.class)};
    private static final String[] EXCEPTION = new String[]{Type.getInternalName(Throwable.class)};

    private static final AtomicInteger LISTENER_ID = new AtomicInteger(0);

    public static <T> EventListener<T> generateListener(@NotNull Object instance, @NotNull java.lang.reflect.Method listenerMethod) {
        String name = listenerMethod.getDeclaringClass().getName() + "Listener" + LISTENER_ID.incrementAndGet();
        ClassWriter writer = GeneratorAdapter.newClassWriter(name, INTERFACES);

        GeneratorAdapter.writeConstructor(writer);
        Type listenerType = Type.getType(listenerMethod.getDeclaringClass());
        Type eventType = Type.getType(listenerMethod.getParameterTypes()[0]);

        // generate handle method
        GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, "invokeASMEvent", "(Ljava/lang/Object;Ljava/lang/Object;)V", EXCEPTION);

        if (!Modifier.isStatic(listenerMethod.getModifiers())) {
            adapter.loadArg(0);
            adapter.checkCast(listenerType);
        }
        adapter.loadArg(1);
        adapter.checkCast(eventType);
        if (Modifier.isStatic(listenerMethod.getModifiers())) {
            adapter.invokeStatic(listenerType, Method.getMethod(listenerMethod));
        } else {
            adapter.invokeVirtual(listenerType, Method.getMethod(listenerMethod));
        }
        adapter.returnValue();
        adapter.endMethod();
        writer.visitEnd();
        byte[] generated = writer.toByteArray();
        try {
            return GeneratedClassDefiner
                    .define(listenerMethod.getDeclaringClass().getClassLoader(), name, generated)
                    .asSubclass(ASMEventExecutor.class)
                    .newInstance()
                    .bindTo(instance);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e); // should never happen as a matter of fact unless something stupid happens...
        }
    }


}
