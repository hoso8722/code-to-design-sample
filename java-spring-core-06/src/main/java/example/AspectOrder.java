// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.AspectOrder
package example;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;

public class AspectOrder {
    public static class Service {
        public String run() {
            System.out.println("    本体");
            return "done";
        }
    }

    @Aspect
    @Order(1)
    public static class Outer {
        @Around("execution(* example.AspectOrder.Service.run(..))")
        public Object around(ProceedingJoinPoint jp) throws Throwable {
            System.out.println("  Outer(@Order(1)): 前");
            try {
                return jp.proceed();
            } finally {
                System.out.println("  Outer(@Order(1)): 後");
            }
        }
    }

    @Aspect
    @Order(2)
    public static class Inner {
        @Around("execution(* example.AspectOrder.Service.run(..))")
        public Object around(ProceedingJoinPoint jp) throws Throwable {
            System.out.println("  Inner(@Order(2)): 前");
            try {
                return jp.proceed();
            } finally {
                System.out.println("  Inner(@Order(2)): 後");
            }
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {
        // 登録の順序は、わざと Inner を先にする
        @Bean
        Inner inner() {
            return new Inner();
        }

        @Bean
        Outer outer() {
            return new Outer();
        }

        @Bean
        Service service() {
            return new Service();
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            System.out.println("Service.run を呼ぶ");
            context.getBean(Service.class).run();
        }
    }
}
