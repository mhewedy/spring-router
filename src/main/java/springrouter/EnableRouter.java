package springrouter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RouterConfiguration.class)
@Documented
public @interface EnableRouter {

    String router() default "router.txt";

    String controllerPackage();

    String modelPackage() default "";
}
