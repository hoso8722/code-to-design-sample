// 実行: java NetworkChecklistScan.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class NetworkChecklistScan {
    // シリーズで再現した問題の「兆候」を、文字列で探すだけの粗い点検。見つかった行は、人が確かめる
    record Rule(String id, String owasp, Pattern pattern, String advice) {}

    static final List<Rule> RULES = List.of(
            new Rule("#01", "A04", Pattern.compile("checkServerTrusted\\([^)]*\\)\\s*\\{\\s*\\}"), "証明書を検証しないTrustManager。信頼ストアに証明書を加える"),
            new Rule("#01", "A04", Pattern.compile("HostnameVerifier|\\(\\w+, \\w+\\) -> true"), "ホスト名の検証を差し替えている。無効にしていないか確かめる"),
            new Rule("#01", "A04", Pattern.compile("disableHostnameVerification"), "ホスト名の検証を切るプロパティ。本番では使わない"),
            new Rule("#02", "A01", Pattern.compile("followRedirects\\(.*(NORMAL|ALWAYS)\\)"), "リダイレクトを自動で辿っている。転送先も検査しているか確かめる"),
            new Rule("#05", "A03", Pattern.compile("<version>(\\[|\\(|LATEST|RELEASE)|-SNAPSHOT"), "版が範囲、最新、SNAPSHOTで指定されている。固定した版にする"),
            new Rule("#05", "A03", Pattern.compile("<url>http://(?!localhost|127\\.0\\.0\\.1)"), "暗号化されないリポジトリのURL。httpsにする"));

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("scan-sample-network");
        Files.writeString(dir.resolve("BadClient.java"), """
                import java.net.http.HttpClient;
                import javax.net.ssl.*;
                import java.security.cert.X509Certificate;

                class BadClient {
                    HttpClient insecure() throws Exception {
                        TrustManager trustAll = new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] c, String t) {}
                            public void checkServerTrusted(X509Certificate[] c, String t) {}
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        };
                        SSLContext context = SSLContext.getInstance("TLS");
                        context.init(null, new TrustManager[] {trustAll}, null);
                        HttpsURLConnection.setDefaultHostnameVerifier((host, session) -> true);
                        return HttpClient.newBuilder().sslContext(context)
                                .followRedirects(HttpClient.Redirect.ALWAYS).build();
                    }
                }
                """);
        Files.writeString(dir.resolve("pom.xml"), """
                <dependencies>
                    <dependency>
                        <artifactId>log4j-api</artifactId>
                        <version>[2.25.0,2.26)</version>
                    </dependency>
                    <dependency>
                        <artifactId>internal-lib</artifactId>
                        <version>1.0-SNAPSHOT</version>
                    </dependency>
                </dependencies>
                <repositories>
                    <repository>
                        <url>http://repo.internal.example/maven</url>
                    </repository>
                </repositories>
                """);

        try (var files = Files.list(dir)) {
            for (Path file : files.sorted().toList()) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    for (Rule rule : RULES) {
                        if (rule.pattern().matcher(lines.get(i)).find()) {
                            System.out.printf("%s:%d [%s/%s] %s%n    %s%n",
                                    file.getFileName(), i + 1, rule.id(), rule.owasp(), rule.advice(), lines.get(i).strip());
                        }
                    }
                }
            }
        }
    }
}
