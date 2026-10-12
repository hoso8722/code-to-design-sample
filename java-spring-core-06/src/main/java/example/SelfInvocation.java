// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.SelfInvocation
package example;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class SelfInvocation {
    public static class OrderService {
        // 外から呼ばれる入り口
        public String process() {
            return audit("this経由");
        }

        public String audit(String via) {
            return "audit が実行された(" + via + ")";
        }
    }

    // 呼び出しを、別のBeanに分けた形
    public static class OrderFacade {
        private final OrderService orders;

        public OrderFacade(OrderService orders) {
            this.orders = orders;
        }

        public String process() {
            return orders.audit("別のBean経由");
        }
    }

    // 自分自身のプロキシを、ObjectProviderで受け取る形
    public static class SelfAwareService {
        private final ObjectProvider<SelfAwareService> self;

        public SelfAwareService(ObjectProvider<SelfAwareService> self) {
            this.self = self;
        }

        public String process() {
            return self.getObject().audit("自分のプロキシ経由");
        }

        public String audit(String via) {
            return "audit が実行された(" + via + ")";
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {
        @Bean
        AuditAspect recorder() {
            return new AuditAspect();
        }

        @Bean
        OrderService orderService() {
            return new OrderService();
        }

        @Bean
        OrderFacade orderFacade(OrderService orders) {
            return new OrderFacade(orders);
        }

        @Bean
        SelfAwareService selfAwareService(ObjectProvider<SelfAwareService> self) {
            return new SelfAwareService(self);
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            OrderService orders = context.getBean(OrderService.class);
            System.out.println("1. 外から audit を直接呼ぶ");
            System.out.println("  " + orders.audit("外から直接"));
            System.out.println("2. process の中から this で audit を呼ぶ(自己呼び出し)");
            System.out.println("  " + orders.process());
            System.out.println("3. 別のBeanから audit を呼ぶ");
            System.out.println("  " + context.getBean(OrderFacade.class).process());
            System.out.println("4. 自分のプロキシ(ObjectProvider)から audit を呼ぶ");
            System.out.println("  " + context.getBean(SelfAwareService.class).process());
        }
    }
}
