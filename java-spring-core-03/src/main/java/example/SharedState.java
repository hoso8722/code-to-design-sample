// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.SharedState
package example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class SharedState {
    // singletonが、可変の状態を持っている
    static class UnsafeCounter {
        private int count = 0;

        void increment() {
            count++;
        }

        int get() {
            return count;
        }
    }

    // 状態を、スレッドセーフな型にする
    static class SafeCounter {
        private final AtomicInteger count = new AtomicInteger();

        void increment() {
            count.incrementAndGet();
        }

        int get() {
            return count.get();
        }
    }

    @Configuration
    static class Config {
        @Bean
        UnsafeCounter unsafe() {
            return new UnsafeCounter();
        }

        @Bean
        SafeCounter safe() {
            return new SafeCounter();
        }
    }

    static void hammer(Runnable task) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread t = Thread.ofPlatform().start(() -> {
                for (int j = 0; j < 100_000; j++) task.run();
            });
            threads.add(t);
        }
        for (Thread t : threads) t.join();
    }

    public static void main(String[] args) throws Exception {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            UnsafeCounter unsafe = context.getBean(UnsafeCounter.class);
            SafeCounter safe = context.getBean(SafeCounter.class);
            hammer(unsafe::increment);
            hammer(safe::increment);
            System.out.println("4スレッドが、10万回ずつ増やした(期待値は 400000)");
            System.out.println("可変の状態(int):       " + unsafe.get());
            System.out.println("AtomicInteger:         " + safe.get());
        }
    }
}
