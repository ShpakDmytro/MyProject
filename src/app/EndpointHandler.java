package app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface EndpointHandler {
    String method();
    String endpoint();
}