package springbook.learningtest;

import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;


import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LearningTest {

    @Test
    public void invokeMethod() throws Exception {
        String name = "Spring";

        //length()
        assertThat(name.length(), is(6));

        Method lengthMethod = String.class.getMethod("length");
        assertThat((Integer)lengthMethod.invoke(name), is(6));

        //charAt()
        assertThat(name.charAt(0), is('S'));

        Method chartAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character)chartAtMethod.invoke(name, 0), is('S'));
    }

    public interface TargetInterface {
        void hello();

        void hello(String a);
        int minus(int a, int b) throws RuntimeException;
        int plus(int a, int b);
    }

    public class Target implements TargetInterface {

        @Override
        public void hello() {}

        @Override
        public void hello(String a) {}

        @Override
        public int minus(int a, int b) throws RuntimeException { return 0; }

        @Override
        public int plus(int a, int b) { return 0; }

        public void method() {}
    }

    public class Bean {
        public void method() throws RuntimeException {}
    }

    @Test
    public void methodSignaturePointcut() throws SecurityException, NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut(); // 의존성에러
        pointcut.setExpression("execution(public int " + "springbook.learningtest.Target.minus(int,int) " + "throws java.lang.RuntimeException)");
        // 리턴 타입에 관계없이 메소드 선정 : excution(* minus(int,int))
        // 파라미터 갯수와 타입 무시하여 선정 : execution(* minus(..))
        // 메소드 이름도 상관없이 선정 :  execution(* *(..))

        System.out.println(Target.class.getMethod("minus", int.class, int.class));
        // 출력결과 : public int springbook.learningtest.spring.pointcut.Target.minus(int,int) throws java.lang.RuntimeException

        // Target.minus()
        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(Target.class.getMethod("minus", int.class, int.class), null),
                is(true));

        //Target.plus()
        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("plus", int.class, int.class), null), is(false));

        //Bean.method(), classFilter에서 걸러져서 false
        assertThat(pointcut.getClassFilter().matches(Bean.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("method"), null), is(false));
    }

}
