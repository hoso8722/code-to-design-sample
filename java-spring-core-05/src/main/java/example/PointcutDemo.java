// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.PointcutDemo
package example;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class PointcutDemo {
    // ポイントカットの式を変えて、どのメソッドに適用されるかを調べる
    @Aspect
    public static class Probe {
        @Before("execution(* example.LoanService.lend(..))")
        public void onlyLend(JoinPoint jp) {
            System.out.println("  (A) lend だけを対象: " + jp.getSignature().getName());
        }

        @Before("execution(* example.LoanService.*(..))")
        public void allMethods(JoinPoint jp) {
            System.out.println("  (B) LoanService の全メソッドを対象: " + jp.getSignature().getName());
        }

        @Before("execution(* example.LoanService.give*(..))")
        public void giveOnly(JoinPoint jp) {
            System.out.println("  (C) give で始まるメソッドを対象: " + jp.getSignature().getName());
        }

        @Before("execution(* example.ReportService.*(..))")
        public void otherClass(JoinPoint jp) {
            System.out.println("  (D) ReportService を対象: " + jp.getSignature().getName());
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class Config {
        @Bean
        LoanService loanService() {
            return new LoanServiceImpl();
        }

        @Bean
        ReportService reportService() {
            return new ReportService();
        }

        @Bean
        Probe probe() {
            return new Probe();
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            LoanService service = context.getBean(LoanService.class);
            System.out.println("lend を呼ぶ");
            service.lend("sora", "こころ");
            System.out.println("giveBack を呼ぶ");
            service.giveBack("sora", "こころ");
            System.out.println("ReportService.report を呼ぶ");
            context.getBean(ReportService.class).report();
        }
    }
}
