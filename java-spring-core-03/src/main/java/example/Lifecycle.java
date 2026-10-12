// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.Lifecycle
package example;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

public class Lifecycle {
    // 作られてから壊されるまでを、ログに出すBean
    static class Resource {
        final String name;

        Resource(String name) {
            this.name = name;
            System.out.println("  [" + name + "] コンストラクタ");
        }

        @PostConstruct
        void open() {
            System.out.println("  [" + name + "] @PostConstruct(初期化)");
        }

        @PreDestroy
        void close() {
            System.out.println("  [" + name + "] @PreDestroy(破棄)");
        }
    }

    @Configuration
    static class Config {
        @Bean
        Resource eager() {
            return new Resource("singleton");
        }

        @Bean
        @Lazy
        Resource lazy() {
            return new Resource("lazy");
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        Resource proto() {
            return new Resource("prototype");
        }
    }

    public static void main(String[] args) {
        System.out.println("コンテナを起動する");
        var context = new AnnotationConfigApplicationContext(Config.class);
        System.out.println("コンテナの起動が終わった");

        System.out.println("-- singleton を2回取り出す");
        Resource s1 = context.getBean("eager", Resource.class);
        Resource s2 = context.getBean("eager", Resource.class);
        System.out.println("  同じインスタンスか: " + (s1 == s2));

        System.out.println("-- lazy を初めて取り出す");
        context.getBean("lazy", Resource.class);

        System.out.println("-- prototype を2回取り出す");
        Resource p1 = context.getBean("proto", Resource.class);
        Resource p2 = context.getBean("proto", Resource.class);
        System.out.println("  同じインスタンスか: " + (p1 == p2));

        System.out.println("コンテナを閉じる");
        context.close();
        System.out.println("コンテナを閉じ終わった");
    }
}
