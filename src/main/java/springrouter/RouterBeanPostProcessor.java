package springrouter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class RouterBeanPostProcessor implements BeanPostProcessor {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Nullable
    private String modelPackage;

    private final Resource routingResource;
    private final String controllerPackage;

    public RouterBeanPostProcessor(Resource routingResource, String controllerPackage) {
        Assert.notNull(routingResource, "'routingResource' cannot be null");
        Assert.notNull(controllerPackage, "'controllerPackage' cannot be null");

        this.routingResource = routingResource;
        this.controllerPackage = controllerPackage;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean.getClass().isAssignableFrom(RequestMappingHandlerMapping.class)) {
            new RouteMapping((RequestMappingHandlerMapping) bean).register();
        }

        return bean;
    }

    private class RouteMapping {
        private final RequestMappingHandlerMapping bean;

        private RouteMapping(RequestMappingHandlerMapping bean) {
            this.bean = bean;
        }

        private void register() {
            String routes = readRoutes();
            log.trace("router file contents:\n{}", routes);

            Arrays.stream(routes.split("\n"))
                    .map(this::parse)
                    .filter(Objects::nonNull)
                    .forEach(it ->
                            bean.registerMapping(it.requestMappingInfo, it.handler, it.method));
        }

        private String readRoutes() {
            try (InputStream routingStream = routingResource.getInputStream()) {
                log.info("loading routes from: {}", routingResource.getURI());
                return StreamUtils.copyToString(routingStream, Charset.forName("utf8"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private Holder parse(String lineString) {
            if (lineString.trim().startsWith("#")) {// is a comment line
                return null;
            }
            if (lineString.trim().length() == 0) {// is empty line
                return null;
            }

            Line line = new Line(lineString);
            RequestMappingInfo mappingInfo = RequestMappingInfo.paths(line.url)
                    .methods(RequestMethod.valueOf(line.verb))
                    .build();

            return new Holder(line.controller.getMethod(), line.controller.getHandler(),
                    mappingInfo);
        }
    }

    private class Line {
        private final String verb, url;
        private final Controller controller;

        private Line(String lineString) {
            String[] strings = lineString.split("\\s+");
            this.verb = strings[0];
            this.url = strings[1];

            String controllerString = Arrays
                    .stream(strings, 2, strings.length)
                    .collect(joining());
            this.controller = new Controller(controllerString);
        }
    }

    private class Controller {
        private final String method;
        private final Class<?>[] argsTypes;
        private final Class<?> handlerClass;

        private Controller(String controllerString) {
            String[] strings = controllerString.split("#");

            handlerClass = forName(strings[0].trim(), true, false);

            String[] methodWithArgs = strings[1].split("\\(");
            this.method = methodWithArgs[0];

            if (methodWithArgs.length > 1) {
                String args = methodWithArgs[1];
                argsTypes = Arrays.stream(args.replace(')', '\0').split(","))
                        .map(className -> forName(className.trim(), false, true))
                        .toArray(Class[]::new);
            } else {
                argsTypes = null;
            }
        }

        private Object getHandler() {
            try {
                return ReflectionUtils.accessibleConstructor(handlerClass).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Method getMethod() {
            Method method = ReflectionUtils.findMethod(handlerClass, this.method, argsTypes);
            if (method == null) {
                String argsTypesString = this.argsTypes == null ? "" : Arrays.toString(this.argsTypes);
                throw new RuntimeException("method : " +
                        this.method + "(" + argsTypesString + ")" +
                        " in type: " + handlerClass.getName() + " not defined");
            }
            return method;
        }

        private Class<?> forName(String className, boolean appendControllerPackage,
                                 boolean appendModelPackage) {
            try {
                return ClassUtils.forName(className, getClass().getClassLoader());
            } catch (Exception ex) {
                // ignore
            }
            try {
                return ClassUtils.forName("java.lang." + className, getClass().getClassLoader());
            } catch (Exception ex) {
                // ignore
            }
            if (appendControllerPackage) {
                try {
                    return ClassUtils.forName(controllerPackage + "." + className, getClass().getClassLoader());
                } catch (Exception ex) {
                    // ignore
                }
            }
            if (appendModelPackage && modelPackage != null) {
                try {
                    return ClassUtils.forName(modelPackage + "." + className, getClass().getClassLoader());
                } catch (Exception ex) {
                    // ignore
                }
            }
            throw new RuntimeException(className + " not found");
        }
    }

    private static class Holder {
        private final Method method;
        private final Object handler;
        private final RequestMappingInfo requestMappingInfo;

        private Holder(Method method, Object handler, RequestMappingInfo requestMappingInfo) {
            this.method = method;
            this.handler = handler;
            this.requestMappingInfo = requestMappingInfo;
        }
    }
}
