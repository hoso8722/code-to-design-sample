// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.PrototypeInSingleton
package example;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

public class PrototypeInSingleton {
    static final AtomicInteger created = new AtomicInteger();

    // 呼び出しごとに、別のオブジェクトであってほしい(利用者ごとの状態を持つ想定)
    static class Session {
        final int id = created.incrementAndGet();
    }

    // 直接注入:singletonに、1回だけ注入される
    static class DirectService {
        private final Session session;

        DirectService(Session session) {
            this.session = session;
        }

        int sessionId() {
            return session.id;
        }
    }

    // ObjectProvider:使うたびに、コンテナから取り出す
    static class ProviderService {
        private final ObjectProvider<Session> provider;

        ProviderService(ObjectProvider<Session> provider) {
            this.provider = provider;
        }

        int sessionId() {
            return provider.getObject().id;
        }
    }

    @Configuration
    static class Config {
        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        Session session() {
            return new Session();
        }

        @Bean
        DirectService direct(Session session) {
            return new DirectService(session);
        }

        @Bean
        ProviderService viaProvider(ObjectProvider<Session> provider) {
            return new ProviderService(provider);
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            DirectService direct = context.getBean(DirectService.class);
            ProviderService provider = context.getBean(ProviderService.class);
            System.out.print("直接注入(3回呼ぶ):    ");
            for (int i = 0; i < 3; i++) System.out.print("session#" + direct.sessionId() + " ");
            System.out.println();
            System.out.print("ObjectProvider(3回):  ");
            for (int i = 0; i < 3; i++) System.out.print("session#" + provider.sessionId() + " ");
            System.out.println();
            System.out.println("作られたSessionの数: " + created.get());
        }
    }
}
