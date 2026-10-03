// 実行: java StartupApp.java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 起動直後に、いろいろなJDKの機能を少しずつ使う「小さなアプリケーション」
public class StartupApp {
    public static void main(String[] args) {
        long start = System.nanoTime();

        // 1. HTTP のリクエストを組み立てる（送信はしない）
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://example.com/api")).GET().build();

            // 2. 日付の文字列を作って、正規表現で年月ごとに数える
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Pattern pattern = Pattern.compile("(\\d{4})-(\\d{2})-\\d{2}");
            Map<String, Long> counts = IntStream.range(0, 20_000)
                    .mapToObj(i -> LocalDate.of(2026, 1, 1).plusDays(i % 365).format(format))
                    .map(pattern::matcher)
                    .filter(m -> m.matches())
                    .collect(Collectors.groupingBy(m -> m.group(1) + "-" + m.group(2), TreeMap::new, Collectors.counting()));

            System.out.printf("%s %s、%d か月ぶん、main の処理 %d ms%n",
                    request.method(), request.uri().getHost(), counts.size(), (System.nanoTime() - start) / 1_000_000);
        }
    }
}
