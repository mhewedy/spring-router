package springrouter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.lang.reflect.Method;

public class ResponseBodyBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        this.advisor = new RequestResponseBodyMethodProcessorAdvisor();
    }

    private class RequestResponseBodyMethodProcessorAdvisor extends StaticMethodMatcherPointcutAdvisor {

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return targetClass.isAssignableFrom(RequestResponseBodyMethodProcessor.class) &&
                    "supportsReturnType".equals(method.getName());
        }

        @Override
        public Advice getAdvice() {
            return new RequestResponseBodyMethodProcessorAdvice();
        }
    }

    private class RequestResponseBodyMethodProcessorAdvice implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            log.trace("invoking " + invocation.getMethod().toString() + " -> true");
            return true;
        }
    }
}
