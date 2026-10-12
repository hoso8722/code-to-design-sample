// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.Scenarios
package example;

import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

public class Scenarios {
    // 1. Mailerを登録しないまま、Notifierを登録する
    @Configuration
    static class MissingConfig {
        @Bean
        CtorNotifier notifier(Mailer mailer) {
            return new CtorNotifier(mailer);
        }
    }

    // 2. Mailerの実装が2つある
    @Configuration
    static class TwoMailersConfig {
        @Bean
        Mailer smtpMailer() {
            return (to, message) -> "smtp";
        }

        @Bean
        Mailer fakeMailer() {
            return new FakeMailer();
        }

        @Bean
        CtorNotifier notifier(Mailer mailer) {
            return new CtorNotifier(mailer);
        }
    }

    // 2-1. @Primaryで、既定の実装を決める
    @Configuration
    static class PrimaryConfig {
        @Bean
        @Primary
        Mailer smtpMailer() {
            return (to, message) -> "smtp が使われた: " + to;
        }

        @Bean
        Mailer fakeMailer() {
            return new FakeMailer();
        }

        @Bean
        CtorNotifier notifier(Mailer mailer) {
            return new CtorNotifier(mailer);
        }
    }

    // 2-2. @Qualifierで、名前を指定する
    @Configuration
    static class QualifierConfig {
        @Bean
        Mailer smtpMailer() {
            return (to, message) -> "smtp";
        }

        @Bean
        Mailer fakeMailer() {
            return new FakeMailer();
        }

        @Bean
        CtorNotifier notifier(@Qualifier("fakeMailer") Mailer mailer) {
            return new CtorNotifier(mailer);
        }
    }

    // 3. 循環:コンストラクタ注入
    static class CtorA {
        CtorA(CtorB b) {}
    }

    static class CtorB {
        CtorB(CtorA a) {}
    }

    @Configuration
    static class CtorCycleConfig {
        @Bean
        CtorA ctorA(CtorB b) {
            return new CtorA(b);
        }

        @Bean
        CtorB ctorB(CtorA a) {
            return new CtorB(a);
        }
    }

    // 3-1. 循環:フィールド注入
    static class FieldA {
        @Autowired
        FieldB b;
    }

    static class FieldB {
        @Autowired
        FieldA a;
    }

    @Configuration
    static class FieldCycleConfig {
        @Bean
        FieldA fieldA() {
            return new FieldA();
        }

        @Bean
        FieldB fieldB() {
            return new FieldB();
        }
    }

    static void run(String label, Class<?> config, Function<ApplicationContext, String> after) {
        System.out.println("## " + label);
        try (var context = new AnnotationConfigApplicationContext(config)) {
            System.out.println("起動できた");
            System.out.println(after.apply(context));
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            System.out.println("起動に失敗: " + e.getClass().getSimpleName());
            System.out.println("根本の原因: " + root.getClass().getSimpleName());
            String message = root.getMessage();
            System.out.println("メッセージ: " + message.lines().findFirst().orElse(""));
        }
    }

    public static void main(String[] args) {
        run("1. 依存するBeanが登録されていない", MissingConfig.class, c -> "");
        run("2. 同じ型のBeanが2つある", TwoMailersConfig.class, c -> "");
        run("2-1. @Primaryで決める", PrimaryConfig.class, c -> c.getBean(CtorNotifier.class).notifyLoan("sora"));
        run("2-2. @Qualifierで決める", QualifierConfig.class, c -> c.getBean(CtorNotifier.class).notifyLoan("sora"));
        run("3. 循環参照(コンストラクタ注入)", CtorCycleConfig.class, c -> "");
        run("3-1. 循環参照(フィールド注入)", FieldCycleConfig.class,
                c -> "FieldAのbとFieldBのaが、互いを指している: "
                        + (c.getBean(FieldA.class).b == c.getBean(FieldB.class)
                                && c.getBean(FieldB.class).a == c.getBean(FieldA.class)));
    }
}
