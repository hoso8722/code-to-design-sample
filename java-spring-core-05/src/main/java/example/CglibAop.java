// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.CglibAop
package example;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class CglibAop {
    @Configuration
    @ComponentScan(basePackageClasses = LoanService.class,
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = {LoanServiceImpl.class, TimingAspect.class, ReportService.class}))
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class ClassProxyConfig {}

    @Configuration
    @ComponentScan(basePackageClasses = LoanService.class,
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = {ReportService.class, TimingAspect.class}))
    @EnableAspectJAutoProxy
    static class DefaultConfig {}

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(ClassProxyConfig.class)) {
            Object service = context.getBean(LoanService.class);
            System.out.println("proxyTargetClass = true のとき、LoanService のBean:");
            System.out.println("  クラス: " + service.getClass().getName());
            System.out.println("  CGLIB: " + AopUtils.isCglibProxy(service));
            System.out.println("  実装のクラスで取り出せるか: " + (context.getBean(LoanServiceImpl.class) != null));
        }
        try (var context = new AnnotationConfigApplicationContext(DefaultConfig.class)) {
            Object report = context.getBean(ReportService.class);
            System.out.println("interface を持たないクラスのBean(既定の設定):");
            System.out.println("  クラス: " + report.getClass().getName());
            System.out.println("  CGLIB: " + AopUtils.isCglibProxy(report));
        }
    }
}
