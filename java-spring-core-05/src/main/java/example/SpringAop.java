// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.SpringAop
package example;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class SpringAop {
    @Configuration
    @ComponentScan(basePackageClasses = LoanService.class,
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = {LoanServiceImpl.class, TimingAspect.class}))
    @EnableAspectJAutoProxy
    static class Config {}

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            LoanService service = context.getBean(LoanService.class);
            System.out.println(service.lend("sora", "こころ"));
            System.out.println(service.giveBack("sora", "こころ"));

            System.out.println("取り出したBeanのクラス: " + service.getClass().getName());
            System.out.println("  JDKの動的プロキシか: " + AopUtils.isJdkDynamicProxy(service));
            System.out.println("  CGLIBのプロキシか:   " + AopUtils.isCglibProxy(service));
            System.out.println("  元のクラス:           " + AopProxyUtils.ultimateTargetClass(service).getName());
            try {
                context.getBean(LoanServiceImpl.class);
            } catch (Exception e) {
                System.out.println("実装のクラス(LoanServiceImpl)で取り出すと: " + e.getClass().getSimpleName());
            }
        }
    }
}
