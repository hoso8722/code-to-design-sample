// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.StackDepth
package example;

import java.util.Arrays;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class StackDepth {
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {
        @Bean
        CrossCuttingAspects.AuditAspect auditAspect() {
            return new CrossCuttingAspects.AuditAspect();
        }

        @Bean
        CrossCutting.BookService bookService() {
            return new CrossCutting.BookService();
        }
    }

    static StackTraceElement[] trace(Runnable action) {
        try {
            action.run();
            return new StackTraceElement[0];
        } catch (RuntimeException e) {
            return e.getStackTrace();
        }
    }

    static long frameworkFrames(StackTraceElement[] frames) {
        return Arrays.stream(frames)
                .filter(f -> f.getClassName().startsWith("org.springframework") || f.getClassName().startsWith("org.aspectj"))
                .count();
    }

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream())); // 監査の出力は捨てる
        var direct = new CrossCutting.BookService();
        StackTraceElement[] a = trace(() -> direct.remove(-1));
        StackTraceElement[] b;
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var proxied = context.getBean(CrossCutting.BookService.class);
            b = trace(() -> proxied.remove(-1));
        }
        System.setOut(System.err);
        System.err.println("例外のスタックトレースのフレーム数");
        System.err.println("  アスペクトなし(newしたオブジェクトを直接呼ぶ): " + a.length + " (SpringとAspectJのフレーム: " + frameworkFrames(a) + ")");
        System.err.println("  アスペクトあり(プロキシ経由):                 " + b.length + " (SpringとAspectJのフレーム: " + frameworkFrames(b) + ")");
    }
}
