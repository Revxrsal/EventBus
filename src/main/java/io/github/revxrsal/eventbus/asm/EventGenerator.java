package io.github.revxrsal.eventbus.asm;

import io.github.revxrsal.eventbus.gen.Index;
import io.github.revxrsal.eventbus.gen.RequireNonNull;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.github.revxrsal.eventbus.asm.GeneratorAdapter.DEFAULT_CONSTRUCTOR;
import static io.github.revxrsal.eventbus.asm.GeneratorAdapter.OBJECT_TYPE;

final class EventGenerator implements Opcodes {

    private static final Map<Class<?>, GeneratedEventFactory> FACTORIES = new ConcurrentHashMap<>();

    private static final String GEN_FACTORY = Type.getInternalName(GeneratedEventFactory.class);
    private static final Type OBJECTS = Type.getType(Objects.class);
    private static final Type EQUALS_BUILDER = Type.getType(GeneratedEqualsBuilder.class);
    private static final Type TO_STRING_BUILDER = Type.getType(GeneratedToStringBuilder.class);
    private static final Method REQ_NON_NULL = Method.getMethod("Object requireNonNull(java.lang.Object, java.lang.String)");
    private static final Method APPEND = Method.getMethod(GeneratedEqualsBuilder.class.getName() + " append(java.lang.Object, java.lang.Object)");
    private static final Method APPEND_SUPER = Method.getMethod(GeneratedEqualsBuilder.class.getName() + " appendSuper(boolean)");
    private static final Method IS_EQUAL = Method.getMethod("boolean isEquals()");
    private static final Method HASH = Method.getMethod("int hash(java.lang.Object[])");
    private static final Method EQUALS = Method.getMethod("boolean equals(java.lang.Object)");
    private static final Method TO_STRING_BUILDER_CONSTRUCTOR = Method.getMethod("void <init>(java.lang.String)");
    private static final Method TO_STRING_APPEND = Method.getMethod("void append(java.lang.String, java.lang.Object)");
    private static final Method TO_STRING = Method.getMethod("java.lang.String toString()");

    public static <T, U extends T> U generate(@NotNull Class<T> event, Object... arguments) {
        return (U) generateFactory(event).newEvent(arguments);
    }

    public static GeneratedEventFactory generateFactory(@NotNull Class<?> eventType) {
        if (!eventType.isInterface()) {
            throw new IllegalArgumentException("Event class must be an interface!");
        }
        return FACTORIES.computeIfAbsent(eventType, eventClass -> {
            String name = eventClass.getPackage().getName() + ".gen." + eventClass.getSimpleName();
            Type genType = Type.getType("L" + name.replace('.', '/') + ";");
            ClassWriter writer = GeneratorAdapter.newClassWriter(name, Type.getInternalName(eventClass));
            List<CtrType> constructorTypes = new ArrayList<>();
            FieldVisitor fieldVisitor;
            for (java.lang.reflect.Method method : eventClass.getMethods()) {
                if (method.isDefault()) continue;
                Index indexAnn = method.getAnnotation(Index.class);
                if (indexAnn == null) {
                    throw new IllegalArgumentException("Found an abstract method (" + method.getName() + ") that is not annotated with @Index!");
                }

                String fieldName = getFieldName(method.getName());
                if (!method.getName().startsWith("set")) {
                    if (method.getReturnType() == Void.TYPE) {
                        throw new IllegalArgumentException("Don't know how to implement a void method (" + method.getName() + ")");
                    }
                    Type fieldType = Type.getType(method.getReturnType());
                    fieldVisitor = writer.visitField(ACC_PRIVATE, fieldName, fieldType.getDescriptor(), null, null);
                    fieldVisitor.visitEnd();
                    constructorTypes.add(new CtrType(indexAnn.value(), fieldType, fieldName));
                    // generate getter
                    GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
                    adapter.loadThis();
                    adapter.getField(genType, fieldName, fieldType);
                    adapter.returnValue();
                    adapter.endMethod();
                } else { // method is setter
                    Parameter p = method.getParameters()[0];
                    Type fieldType = Type.getType(p.getType());
                    GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
                    adapter.loadThis();
                    adapter.loadArg(0);

                    if (p.isAnnotationPresent(RequireNonNull.class)) {
                        adapter.push(p.getAnnotation(RequireNonNull.class).value().replace("$field", fieldName));
                        adapter.invokeStatic(OBJECTS, REQ_NON_NULL);
                    }
                    adapter.checkCast(fieldType);
                    adapter.putField(genType, fieldName, fieldType);
                    adapter.returnValue();
                    adapter.endMethod();
                }
            }
            constructorTypes.sort(Comparator.comparingInt(c -> c.index));
            String constructor = "(" + constructorTypes.stream().map(c -> c.type.toString()).collect(Collectors.joining()) + ")V";
            GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, "<init>", constructor);
            {
                adapter.loadThis();
                adapter.invokeConstructor();
                for (int i = 0; i < constructorTypes.size(); i++) {
                    CtrType type = constructorTypes.get(i);
                    adapter.loadThis();
                    adapter.loadArg(i);
                    adapter.putField(genType, type.fieldName, type.type);
                }
                adapter.returnValue();
                adapter.endMethod();
            }

            // generate equals()
            {
                adapter = GeneratorAdapter.newMethodGenerator(writer, "equals", "(Ljava/lang/Object;)Z");
                adapter.visitCode();
                adapter.writeLabel();
                adapter.loadThis();
                adapter.loadArg(0);
                Label label = new Label();
                adapter.ifCmp(genType, GeneratorAdapter.NE, label);
                adapter.push(true);
                adapter.returnValue(); // if (this == object) return true;
                adapter.visitLabel(label);

                adapter.visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                adapter.loadArg(0);
                adapter.instanceOf(genType);
                label = new Label();
                adapter.visitJumpInsn(IFNE, label);
                adapter.push(false); // if (!(anothes instanceof OurEvent)) return false;
                adapter.returnValue();
                adapter.visitLabel(label);
                adapter.visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                adapter.newInstance(EQUALS_BUILDER);
                adapter.dup();
                adapter.invokeConstructor(EQUALS_BUILDER, DEFAULT_CONSTRUCTOR);
                int builderLocal = adapter.newLocal(EQUALS_BUILDER);
                adapter.storeLocal(builderLocal);
                if (eventClass.getInterfaces().length > 0) {
                    adapter.loadLocal(builderLocal);
                    adapter.loadThis();
                    adapter.loadArg(0);
                    adapter.invokeConstructor(OBJECT_TYPE, EQUALS);
                    adapter.invokeVirtual(EQUALS_BUILDER, APPEND_SUPER);
                }
                for (CtrType type : constructorTypes) {
                    adapter.loadLocal(builderLocal);
                    adapter.loadThis();
                    adapter.getField(genType, type.fieldName, type.type);
                    adapter.loadArg(0);
                    adapter.checkCast(genType);
                    adapter.getField(genType, type.fieldName, type.type);
                    adapter.invokeVirtual(EQUALS_BUILDER, APPEND_METHODS.getOrDefault(type.type, APPEND));
                    adapter.pop();
                }
                adapter.writeLabel();
                adapter.loadLocal(builderLocal);
                adapter.invokeVirtual(EQUALS_BUILDER, IS_EQUAL);
                adapter.returnValue();
                adapter.writeLabel();
                adapter.endMethod();
            }

            // generate hashCode()
            {
                adapter = GeneratorAdapter.newMethodGenerator(writer, "hashCode", "()I");
                adapter.push(constructorTypes.size());
                adapter.newArray(OBJECT_TYPE);
                for (int i = 0; i < constructorTypes.size(); i++) {
                    adapter.dup();
                    CtrType type = constructorTypes.get(i);
                    adapter.push(i);
                    adapter.loadThis();
                    adapter.getField(genType, type.fieldName, type.type);
                    adapter.box(type.type);
                    adapter.arrayStore(OBJECT_TYPE);
                }
                adapter.invokeStatic(OBJECTS, HASH);
                adapter.returnValue();
                adapter.endMethod();
            }

            // generate toString()
            {
                adapter = GeneratorAdapter.newMethodGenerator(writer, "toString", "()Ljava/lang/String;");
                adapter.newInstance(TO_STRING_BUILDER);
                adapter.dup();
                adapter.push(eventClass.getSimpleName());
                adapter.invokeConstructor(TO_STRING_BUILDER, TO_STRING_BUILDER_CONSTRUCTOR);
                int localIndex = adapter.newLocal(TO_STRING_BUILDER);
                adapter.storeLocal(localIndex);
                for (CtrType type : constructorTypes) {
                    adapter.loadLocal(localIndex);
                    adapter.push(type.fieldName);
                    adapter.loadThis();
                    adapter.getField(genType, type.fieldName, type.type);
                    adapter.box(type.type);
                    adapter.invokeVirtual(TO_STRING_BUILDER, TO_STRING_APPEND);
                }
                adapter.loadLocal(localIndex);
                adapter.invokeVirtual(TO_STRING_BUILDER, TO_STRING);
                adapter.returnValue();
                adapter.endMethod();
            }
            byte[] generated = writer.toByteArray();

            GeneratedClassDefiner.define(eventClass.getClassLoader(), name, generated);

            // generate a factory to invoke the object constructor
            name = GeneratedEventFactory.class.getPackage().getName() + "_." + eventClass.getSimpleName() + "GeneratedEventFactory";
            writer = GeneratorAdapter.newClassWriter(name, GEN_FACTORY);
            GeneratorAdapter.writeConstructor(writer);
            adapter = GeneratorAdapter.newMethodGenerator(writer, "newEvent", "([Ljava/lang/Object;)Ljava/lang/Object;");
            adapter.newInstance(genType);
            adapter.dup();
            for (int i = 0; i < constructorTypes.size(); i++) {
                CtrType p = constructorTypes.get(i);
                adapter.loadArg(0);
                adapter.push(i);
                adapter.arrayLoad(OBJECT_TYPE);
                if (p.type.getSort() != Type.OBJECT) {
                    adapter.unbox(p.type);
                } else {
                    adapter.checkCast(p.type);
                }
            }
            adapter.invokeConstructor(genType, new Method("<init>", constructor));
            adapter.returnValue();
            adapter.endMethod();

            generated = writer.toByteArray();
            try {
                GeneratedEventFactory factory = GeneratedClassDefiner.define(eventClass.getClassLoader(), name, generated)
                        .asSubclass(GeneratedEventFactory.class)
                        .newInstance();
                return parameters -> {
                    try {
                        return factory.newEvent(parameters);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("Received invalid argument size. Expected: " + constructorTypes.size() + ", found: " + parameters.length);
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException("Received incorrect argument types.", e);
                    }
                };
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        });
    }

    private static String getFieldName(String methodName) {
        if (methodName.length() > 3 && methodName.startsWith("get") || methodName.startsWith("set")) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        }
        if (methodName.length() > 2 && methodName.startsWith("is")) {
            return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        }
        return methodName;
    }

    private static class CtrType {

        private final int index;
        private final Type type;
        private final String fieldName;

        public CtrType(int index, Type type, String fieldName) {
            this.index = index;
            this.type = type;
            this.fieldName = fieldName;
        }
    }

    private static final Map<Type, Method> APPEND_METHODS = new HashMap<>();

    static {
        for (java.lang.reflect.Method method : GeneratedEqualsBuilder.class.getDeclaredMethods()) {
            if (method.getName().equals("append")) {
                Type type = Type.getType(method.getParameterTypes()[0]);
                APPEND_METHODS.put(type, Method.getMethod(method));
            }
        }
    }

}
