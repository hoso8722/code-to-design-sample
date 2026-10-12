// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.spring.SpringMain
package example.spring;

import example.Mailer;
import example.Notifier;
import java.util.Arrays;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringMain {
    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            // コンテナに登録されたBean(Spring自身が使う内部のBeanも含む)
            String[] names = context.getBeanDefinitionNames();
            System.out.println("Beanの数: " + names.length);
            System.out.println("Beanの名前: " + Arrays.toString(names));
            System.out.println("example のBean: " + Arrays.stream(names)
                    .filter(n -> context.getBean(n).getClass().getName().startsWith("example."))
                    .toList());

            // 取り出して使う
            Notifier notifier = context.getBean(Notifier.class);
            notifier.notifyLoan("sora", "こころ");

            // 同じBeanは、何度取り出しても同じインスタンス
            Notifier again = context.getBean(Notifier.class);
            System.out.println("同じインスタンスか: " + (notifier == again));
            Mailer mailer = context.getBean(Mailer.class);
            System.out.println("Mailerの実装: " + mailer.getClass().getName());
        }
    }
}
