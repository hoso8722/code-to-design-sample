// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.ProfileMain
package example.profile;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

public class ProfileMain {
    @Configuration
    @ComponentScan(basePackageClasses = LoanService.class)
    static class Config {}

    static void run(String... profiles) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(profiles);
            context.register(Config.class);
            context.refresh();
            System.out.println("有効なプロファイル: " + (profiles.length == 0 ? "(なし)" : String.join(",", profiles)));
            System.out.println("  Notifierの実装: " + context.getBean(Notifier.class).getClass().getSimpleName());
            System.out.println("  " + context.getBean(LoanService.class).lend("sora", "こころ"));
        }
    }

    public static void main(String[] args) {
        run();
        run("dev");
        run("prod");
    }
}
