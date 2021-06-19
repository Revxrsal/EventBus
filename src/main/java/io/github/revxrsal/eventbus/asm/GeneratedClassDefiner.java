package io.github.revxrsal.eventbus.asm;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class GeneratedClassDefiner {

    public static final GeneratedClassDefiner INSTANCE = new GeneratedClassDefiner();
    private final ConcurrentMap<ClassLoader, GeneratedClassLoader> loaders = new ConcurrentHashMap<>();

    private GeneratedClassDefiner() {
    }

    public static Class<?> define(@NotNull ClassLoader parentLoader, @NotNull String name, byte[] data) {
        return INSTANCE.defineClass(parentLoader, name, data);
    }

    private Class<?> defineClass(ClassLoader parentLoader, String name, byte[] data) {
        final GeneratedClassLoader loader = loaders.computeIfAbsent(parentLoader, GeneratedClassLoader::new);
        synchronized (loader.getClassLoadingLock(name)) {
            if (loader.hasClass(name)) {
                throw new IllegalStateException(name + " has already been defined! This should not happen.");
            }
            final Class<?> c = loader.define(name, data);
            assert c.getName().equals(name);
            return c;
        }
    }

    private static class GeneratedClassLoader extends ClassLoader {

        protected GeneratedClassLoader(final ClassLoader parent) {
            super(parent);
        }

        private Class<?> define(final String name, final byte[] data) {
            synchronized (getClassLoadingLock(name)) {
                assert !hasClass(name);
                final Class<?> c = defineClass(name, data, 0, data.length);
                resolveClass(c);
                return c;
            }
        }

        public Object getClassLoadingLock(final String name) {
            return super.getClassLoadingLock(name);
        }

        public boolean hasClass(final String name) {
            synchronized (getClassLoadingLock(name)) {
                try {
                    Class.forName(name);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
        }

        static {
            ClassLoader.registerAsParallelCapable();
        }
    }
}
