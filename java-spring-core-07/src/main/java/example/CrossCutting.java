// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.CrossCutting
package example;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

public class CrossCutting {
    // 業務のクラス:監査、再試行、握りつぶしの印を付けただけ
    public static class BookService {
        @Audited(action = "貸出")
        public String lend(String user, String title) {
            return user + " が「" + title + "」を借りた";
        }

        @Audited(action = "削除")
        public String remove(long id) {
            if (id < 0) {
                throw new IllegalArgumentException("idが不正: " + id);
            }
            return "削除した id=" + id;
        }
    }

    public static class PaymentGateway {
        private final AtomicInteger calls = new AtomicInteger();

        // 最初の2回は失敗し、3回目に成功する(一時的な障害を模擬)
        @Retry(times = 3)
        public String charge(int yen) {
            int n = calls.incrementAndGet();
            if (n < 3) {
                throw new IllegalStateException("接続がタイムアウト(" + n + "回目)");
            }
            return yen + "円を請求した";
        }
    }

    // 副作用(メールの送信)があって、そのあとで失敗するメソッド
    public static class NoticeService {
        private final AtomicInteger sent = new AtomicInteger();
        private final AtomicInteger calls = new AtomicInteger();

        public int sentCount() {
            return sent.get();
        }

        @Retry(times = 3)
        public String noticeAndRecord() {
            sent.incrementAndGet();
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("記録に失敗(" + calls.get() + "回目)");
            }
            return "通知して、記録した";
        }
    }

    // publicのフィールドと、それを返すメソッドを持つ(プロキシ越しの、フィールドへの直接アクセスを調べる)
    public static class Settings {
        public int limit = 42;

        @Audited(action = "設定の読み取り")
        public int limit() {
            return limit;
        }
    }

    public static class RiskyService {
        @Swallow
        public String doRisky() {
            throw new IllegalStateException("本当は失敗している");
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {
        @Bean
        CrossCuttingAspects.AuditAspect auditAspect() {
            return new CrossCuttingAspects.AuditAspect();
        }

        @Bean
        CrossCuttingAspects.RetryAspect retryAspect() {
            return new CrossCuttingAspects.RetryAspect();
        }

        @Bean
        CrossCuttingAspects.SwallowAspect swallowAspect() {
            return new CrossCuttingAspects.SwallowAspect();
        }

        @Bean
        BookService bookService() {
            return new BookService();
        }

        @Bean
        PaymentGateway paymentGateway() {
            return new PaymentGateway();
        }

        @Bean
        NoticeService noticeService() {
            return new NoticeService();
        }

        @Bean
        Settings settings() {
            return new Settings();
        }

        @Bean
        RiskyService riskyService() {
            return new RiskyService();
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            System.out.println("1. 監査(@Audited)");
            BookService books = context.getBean(BookService.class);
            books.lend("sora", "こころ");
            try {
                books.remove(-1);
            } catch (IllegalArgumentException e) {
                System.out.println("  呼び出し側にも、例外が届いた: " + e.getMessage());
            }

            System.out.println("2. 再試行(@Retry): 冪等な処理");
            System.out.println("  結果: " + context.getBean(PaymentGateway.class).charge(1200));

            System.out.println("3. 再試行(@Retry): 副作用のある処理");
            NoticeService notice = context.getBean(NoticeService.class);
            System.out.println("  結果: " + notice.noticeAndRecord());
            System.out.println("  通知を送った回数: " + notice.sentCount() + " (業務としては1回のはず)");

            System.out.println("4. プロキシ越しに、publicのフィールドを直接読む");
            Settings settings = context.getBean(Settings.class);
            System.out.println("  settings.limit (フィールド): " + settings.limit);
            System.out.println("  settings.limit() (メソッド): " + settings.limit());

            System.out.println("5. 握りつぶすアスペクト(@Swallow)");
            String result = context.getBean(RiskyService.class).doRisky();
            System.out.println("  呼び出し側が受け取った結果: " + result);
        }
    }
}
