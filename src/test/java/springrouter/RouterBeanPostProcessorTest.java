package springrouter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class RouterBeanPostProcessorTest {

    // exception will be thrown if registration fails
    @Test
    public void testBeanPostProcessLoading() {
        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(Config.class);

        RouterBeanPostProcessor beanPostProcessor = applicationContext.getBean(RouterBeanPostProcessor.class);
        Assert.assertThat(beanPostProcessor, is(notNullValue()));

        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Assert.assertThat(handlerMapping, is(notNullValue()));

    }

    @Configuration
    static class Config {
        @Bean
        RouterBeanPostProcessor routerBeanPostProcessor() {
            RouterBeanPostProcessor processor = new RouterBeanPostProcessor(new ClassPathResource("route1.txt"),
                    "springrouter.controllers");
            processor.setModelPackage("springrouter.model");        // optional
            return processor;
        }

        @Bean
        RequestMappingHandlerMapping requestMappingHandlerMapping() {
            return new RequestMappingHandlerMapping();
        }
    }
}