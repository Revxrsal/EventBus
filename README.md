
# EventBus
A blazingly fast, small, multi-threadable and feature-rich event bus for Java

## Features
- Uses ASM to invoke listeners, which performs as fast as direct method invokation, unlike reflection
- Built to be thread-safe and well-supported for use in multi-threaded environments.
- Allows the usage of normal reflection in cases where ASM is not desired.
- Provides easy classes for benchmarking and debugging
- Ability to generate events from interfaces using ASM, which reduces the hassle of writing constructors, null checks, toString()/equals()/hashCode(), etc.

## Usage

### Example event use
```java
EventBus bus = EventBusBuilder.asm() // can be methodHandles() or reflection()    
   .executor(Executors.newSingleThreadExecutor()) // optional    
   .scanAnnotations(MyCustomAnnotation.class) // optional    
   .exceptionHandler(new MyCustomExceptionHandler()) // optional    
   .build();  
  
bus.register(new MyListener());  
  
bus.register(CustomEvent.class, event -> {  
    System.out.println("Woo!");  
    System.out.println(event);  
});  
  
bus.register(CustomEvent.class, new EventListener<CustomEvent>() {  
      @Override  
      public void handle(@NotNull CustomEvent event) throws Throwable {  
          System.out.println("Called once!");  
          bus.unregister(this);  
     }  
 });  
  
bus.post(new CustomEvent(1, "custom property", true))  
  
@SubscribeEvent  
public void onCustomEvent(CustomEvent event) {  
    System.out.println("Hello from onCustomEvent()");  
}
```

### Generated event (requires ASM)
```java
public interface ArrowLaunchEvent {  
  
  @Index(0) String getLauncher();  

  @Index(1) Vector getVelocity();  

  @Index(1) void setVelocity(@RequireNonNull Vector vector);  

}

EventBus bus = ...
bus.post(ArrowLaunchEvent.class, "Hunter", new Vector(1, 1, 1))
```

which will generate the following:
```java
public final class ArrowLaunchEvent implements our.custom.ArrowLaunchEvent {

    private String launcher;
    private Vector velocity;

    public ArrowLaunchEvent(String launcher, Vector velocity) {
        this.launcher = launcher;
        this.velocity = velocity;
    }

    public String getLauncher() {
        return launcher;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector vector) {
        velocity = Objects.requireNonNull(vector, "velocity cannot be null!");
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof ArrowLaunchEvent)) {
            return false;
        } else {
            GeneratedEqualsBuilder builder = new GeneratedEqualsBuilder();
            builder.append(launcher, ((ArrowLaunchEvent) other).launcher);
            builder.append(velocity, ((ArrowLaunchEvent) other).velocity);
            return builder.isEquals();
        }
    }

    public int hashCode() {
        return Objects.hash(launcher, velocity);
    }

    public String toString() {
        GeneratedToStringBuilder builder = new GeneratedToStringBuilder("ArrowLaunchEvent");
        builder.append("launcher", launcher);
        builder.append("velocity", velocity);
        return builder.toString();
    }
}
```