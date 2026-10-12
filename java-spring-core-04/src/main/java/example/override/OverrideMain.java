// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.OverrideMain
package example.override;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class OverrideMain {
    public interface Notifier {
        String notifyLoan(String user, String title);
    }

    public static class LoanService {
        private final Notifier notifier;

        public LoanService(Notifier notifier) {
            this.notifier = notifier;
        }

        public String lend(String user, String title) {
            return notifier.notifyLoan(user, title);
        }
    }

    @Configuration
    static class ProdConfig {
        @Bean
        Notifier notifier() {
            return (user, title) -> "[本番] " + user;
        }

        @Bean
        LoanService loanService(Notifier notifier) {
            return new LoanService(notifier);
        }
    }

    // テスト用の設定:同じ名前(notifier)のBeanを、別の実装で登録する
    @Configuration
    static class TestOverride {
        @Bean
        Notifier notifier() {
            return (user, title) -> "[テスト用の偽物] " + user;
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(ProdConfig.class, TestOverride.class)) {
            System.out.println("同じ名前のBeanを後から登録した結果: " + context.getBean(LoanService.class).lend("sora", "こころ"));
        }
        try (var context = new AnnotationConfigApplicationContext()) {
            context.setAllowBeanDefinitionOverriding(false);
            context.register(ProdConfig.class, TestOverride.class);
            context.refresh();
        } catch (Exception e) {
            System.out.println("上書きを禁止すると: " + e.getClass().getSimpleName());
            String m = e.getMessage();
            System.out.println("  " + m.substring(0, Math.min(m.length(), 118)) + " ...");
        }
    }
}
