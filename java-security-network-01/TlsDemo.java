// 実行: java TlsDemo.java
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TlsDemo {
    static final char[] PASSWORD = "changeit".toCharArray();
    static Path dir;

    // keytool で自己署名の証明書(PKCS12)を作る。san は証明書に書くホスト名
    static Path createKeyStore(String name, String san) throws Exception {
        Path keystore = dir.resolve(name + ".p12");
        String keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
        Process process = new ProcessBuilder(keytool, "-genkeypair", "-alias", name, "-keyalg", "RSA", "-keysize", "2048",
                "-dname", "CN=" + name, "-ext", "san=dns:" + san, "-validity", "30",
                "-keystore", keystore.toString(), "-storetype", "PKCS12", "-storepass", "changeit")
                .redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        if (process.waitFor() != 0) throw new IllegalStateException("keytool が失敗した");
        return keystore;
    }

    static KeyStore load(Path file) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(file)) {
            store.load(in, PASSWORD);
        }
        return store;
    }

    // サーバー側: 鍵ストアの証明書で、HTTPSのサーバーを立てる
    static HttpsServer startServer(Path keystore, String body) throws Exception {
        KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keys.init(load(keystore), PASSWORD);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keys.getKeyManagers(), null, null);
        HttpsServer server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(context));
        server.createContext("/", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server;
    }

    // クライアント側: 信頼ストア(信頼する証明書の集まり)を指定する
    static HttpClient clientTrusting(Path keystore) throws Exception {
        TrustManagerFactory trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(load(keystore)); // 鍵ストアの証明書を、信頼ストアとして使う
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trust.getTrustManagers(), null);
        return HttpClient.newBuilder().sslContext(context).build();
    }

    // 危険: どんな証明書も信頼するTrustManager
    static HttpClient clientTrustingEverything() throws Exception {
        TrustManager trustAll = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        };
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] {trustAll}, null);
        return HttpClient.newBuilder().sslContext(context).build();
    }

    static void get(String label, HttpClient client, HttpsServer server) {
        String url = "https://localhost:" + server.getAddress().getPort() + "/";
        try {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
            System.out.printf("  %-34s 成功 %d %s%n", label, response.statusCode(), response.body());
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            System.out.printf("  %-34s 失敗 %s%n      %s%n", label, e.getClass().getSimpleName(), cause.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        dir = Files.createTempDirectory("tls-demo");
        Path real = createKeyStore("real", "localhost");
        Path fake = createKeyStore("fake", "localhost");
        Path other = createKeyStore("other", "other.example");
        HttpsServer realServer = startServer(real, "本物のサーバー");
        HttpsServer fakeServer = startServer(fake, "偽のサーバー");
        HttpsServer otherServer = startServer(other, "別のホスト名の証明書のサーバー");
        try {
            System.out.println("## 1. 既定のクライアント(自己署名の証明書は、信頼されていない)");
            get("既定のHttpClient → 本物", HttpClient.newHttpClient(), realServer);

            System.out.println("## 2. 信頼ストアに、本物の証明書を入れる");
            get("本物の証明書を信頼 → 本物", clientTrusting(real), realServer);
            get("本物の証明書を信頼 → 偽", clientTrusting(real), fakeServer);

            System.out.println("## 3. すべてを信頼する(危険)");
            get("trust-all → 偽", clientTrustingEverything(), fakeServer);

            System.out.println("## 4. ホスト名の検証(証明書のホスト名は other.example)");
            get("証明書を信頼 → localhost宛て", clientTrusting(other), otherServer);
        } finally {
            realServer.stop(0);
            fakeServer.stop(0);
            otherServer.stop(0);
        }
    }
}
