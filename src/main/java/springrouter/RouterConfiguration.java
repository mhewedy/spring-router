package springrouter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

@Configuration
public class RouterConfiguration implements ImportAware {

    private static final String ROUTER = "router";
    private static final String MODEL_PACKAGE = "modelPackage";
    private static final String CONTROLLER_PACKAGE = "controllerPackage";

    @Nullable
    private AnnotationAttributes enableRouter;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {

        this.enableRouter = AnnotationAttributes.fromMap(importMetadata
                .getAnnotationAttributes(EnableRouter.class.getName()));

        if (this.enableRouter == null) {
            throw new IllegalArgumentException(
                    "@EnableRouter is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Bean
    public RouterBeanPostProcessor routerBeanPostProcessor() {
        final RouterBeanPostProcessor routerBeanPostProcessor = new RouterBeanPostProcessor(
                new ClassPathResource(enableRouter.getString(ROUTER)),
                enableRouter.getString(CONTROLLER_PACKAGE));

        final String modelPackage = enableRouter.getString(MODEL_PACKAGE);
        if (modelPackage != null) {
            routerBeanPostProcessor.setModelPackage(modelPackage);
        }
        return routerBeanPostProcessor;
    }

    @Bean
    public ResponseBodyBeanPostProcessor responseBodyBeanPostProcessor() {
        return new ResponseBodyBeanPostProcessor();
    }
}
