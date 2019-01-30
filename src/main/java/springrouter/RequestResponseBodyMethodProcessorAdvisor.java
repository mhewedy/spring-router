package springrouter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.lang.reflect.Method;

class RequestResponseBodyMethodProcessorAdvisor extends StaticMethodMatcherPointcutAdvisor {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return targetClass.isAssignableFrom(RequestResponseBodyMethodProcessor.class) &&
                "supportsReturnType".equals(method.getName());
    }

    @Override
    public Advice getAdvice() {
        return new RequestResponseBodyMethodProcessorAdvice();
    }

    private static class RequestResponseBodyMethodProcessorAdvice implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return true;
        }
    }

}
