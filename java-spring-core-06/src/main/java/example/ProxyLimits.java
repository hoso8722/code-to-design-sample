// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.ProxyLimits
package example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class ProxyLimits {
    // メソッドの可視性ごとに、アスペクトが効くかを調べる
    public static class Visibility {
        public String auditPublic() {
            return "public";
        }

        protected String auditProtected() {
            return "protected";
        }

        String auditPackage() {
            return "package-private";
        }

        // private メソッドは、外から呼べないので、同じクラスの中から呼ぶ
        private String auditPrivate() {
            return "private";
        }

        public String callPrivate() {
            return auditPrivate();
        }

        public final String auditFinal() {
            return "final";
        }
    }

    public static final class FinalClass {
        public String auditHello() {
            return "hello";
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class VisibilityConfig {
        @Bean
        AuditAspect recorder() {
            return new AuditAspect();
        }

        @Bean
        Visibility visibility() {
            return new Visibility();
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class FinalClassConfig {
        @Bean
        AuditAspect recorder() {
            return new AuditAspect();
        }

        @Bean
        FinalClass finalClass() {
            return new FinalClass();
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(VisibilityConfig.class)) {
            Visibility v = context.getBean(Visibility.class);
            System.out.println("public メソッド:");
            v.auditPublic();
            System.out.println("protected メソッド(同じパッケージから呼ぶ):");
            v.auditProtected();
            System.out.println("package-private メソッド(同じパッケージから呼ぶ):");
            v.auditPackage();
            System.out.println("private メソッド(callPrivate の中から呼ばれる):");
            v.callPrivate();
            System.out.println("final メソッド:");
            v.auditFinal();
        }
        System.out.println("final クラスをBeanにして、アスペクトを適用する:");
        try (var context = new AnnotationConfigApplicationContext(FinalClassConfig.class)) {
            System.out.println("  起動できた");
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            System.out.println("  起動に失敗: " + e.getClass().getSimpleName());
            System.out.println("  根本の原因: " + root.getClass().getSimpleName() + ": " + root.getMessage());
        }
    }
}
