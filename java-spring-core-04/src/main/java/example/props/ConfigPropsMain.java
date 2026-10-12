// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.ConfigPropsMain
package example.props;

import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

public class ConfigPropsMain {
    @Configuration
    @EnableConfigurationProperties(LoanProperties.class)
    static class Config {}

    static void run(String label, Map<String, Object> properties) {
        System.out.println(label);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
            context.register(Config.class);
            context.refresh();
            System.out.println("  " + context.getBean(LoanProperties.class));
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            System.out.println("  起動に失敗: " + e.getClass().getSimpleName());
            System.out.println("  根本の原因: " + root.getClass().getSimpleName() + ": " + root.getMessage());
        }
    }

    public static void main(String[] args) {
        run("設定が正しい(max-count は、ハイフン区切り)", Map.of("app.loan.days", "14", "app.loan.max-count", "5"));
        run("max-count を camelCase で書いた", Map.of("app.loan.days", "14", "app.loan.maxCount", "5"));
        run("days が数値でない", Map.of("app.loan.days", "abc", "app.loan.max-count", "5"));
        run("設定が足りない", Map.of("app.loan.days", "14"));
    }
}
