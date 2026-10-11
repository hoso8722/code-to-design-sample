// 実行: mvn -q compile exec:java -Dexec.mainClass=example.app.Main
package example.app;

import example.common.Money;
import example.domain.Order;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Order order = new Order(List.of(new Money(300), new Money(450)));
        System.out.println("合計: " + order.total().yen() + "円");
    }
}
