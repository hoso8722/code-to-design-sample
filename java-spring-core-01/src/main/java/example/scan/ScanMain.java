// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.scan.ScanMain
package example.scan;

import example.Mailer;
import java.util.Arrays;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

public class ScanMain {
    // @Componentを付けたクラスは、スキャンの対象のパッケージにあれば、Beanとして登録される
    @Component
    public static class ScannedMailer implements Mailer {
        @Override
        public void send(String to, String message) {
            System.out.println("[scan] " + to + " へ送信: " + message);
        }
    }

    @Component
    public static class ScannedNotifier {
        private final Mailer mailer;

        // コンストラクタの引数の型から、Springが、登録済みのBeanを探して渡す
        public ScannedNotifier(Mailer mailer) {
            this.mailer = mailer;
        }

        public void notifyLoan(String user, String title) {
            mailer.send(user, "「" + title + "」を貸し出しました");
        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = ScanMain.class)
    public static class ScanConfig {}

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(ScanConfig.class)) {
            System.out.println("スキャンで登録されたBean: " + Arrays.stream(context.getBeanDefinitionNames())
                    .filter(n -> n.toLowerCase().contains("scanned")).toList());
            context.getBean(ScannedNotifier.class).notifyLoan("sora", "こころ");
        }
    }
}
