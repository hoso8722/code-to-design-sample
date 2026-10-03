// 実行: java HttpHelloTimed.java
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

// HttpHello の各部分の時間を、main の中で計る
public class HttpHelloTimed {
    public static void main(String[] args) {
        long mainStart = ManagementFactory.getRuntimeMXBean().getUptime();  // JVMが動き始めてからの時間
        long t0 = System.nanoTime();
        try (HttpClient client = HttpClient.newHttpClient()) {
            long t1 = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://example.com/")).GET().build();
            long t2 = System.nanoTime();
            System.out.printf("main 開始時の JVM の稼働時間: %d ms、HttpClient を作る: %d ms、HttpRequest を作る: %d ms%n",
                    mainStart, (t1 - t0) / 1_000_000, (t2 - t1) / 1_000_000);
        }
    }
}
