// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.BeanNames
package example;

import java.util.Arrays;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanNames {
    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.refresh();
            Arrays.stream(context.getBeanDefinitionNames()).map(n -> n.substring(n.lastIndexOf('.') + 1)).forEach(n -> System.out.println("  " + n));
        }
    }
}
