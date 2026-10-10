// 実行: java TlsTrust.java
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
import java.security.SecureRandom;
import java.security.cert.Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class TlsTrust {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("tls-demo");
        Path keystoreFile = dir.resolve("server.p12");
        char[] password = Long.toHexString(new SecureRandom().nextLong()).toCharArray(); // この実験だけで使う一時的なパスワード

        try {
            // 実験用の自己署名証明書を、JDKのkeytoolで作る(CN=localhost、有効期間1日)
            Path keytool = Path.of(System.getProperty("java.home"), "bin", "keytool");
            Process p = new ProcessBuilder(keytool.toString(), "-genkeypair", "-alias", "demo",
                    "-keyalg", "RSA", "-keysize", "2048", "-dname", "CN=localhost", "-ext", "san=dns:localhost",
                    "-validity", "1", "-storetype", "PKCS12", "-keystore", keystoreFile.toString(),
                    "-storepass", new String(password)).redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            if (p.waitFor() != 0) {
                throw new IllegalStateException("keytoolの実行に失敗しました");
            }

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream in = Files.newInputStream(keystoreFile)) {
                keyStore.load(in, password);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password);
            SSLContext serverContext = SSLContext.getInstance("TLS");
            serverContext.init(kmf.getKeyManagers(), null, null);

            HttpsServer server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
            server.setHttpsConfigurator(new HttpsConfigurator(serverContext));
            server.createContext("/data", exchange -> {
                byte[] body = "secure hello".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            URI uri = URI.create("https://localhost:" + server.getAddress().getPort() + "/data");
            HttpRequest request = HttpRequest.newBuilder(uri).build();

            // 1. 既定の設定:JDKが信頼する証明書の一覧に、この証明書はない
            try (HttpClient client = HttpClient.newHttpClient()) {
                client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("既定のクライアント: 接続できた");
            } catch (javax.net.ssl.SSLException e) {
                System.out.println("既定のクライアント: " + e.getClass().getSimpleName());
            }

            // 2. この証明書「だけ」を信頼するクライアント:検証は有効のまま
            Certificate certificate = keyStore.getCertificate("demo");
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("demo", certificate);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            SSLContext clientContext = SSLContext.getInstance("TLS");
            clientContext.init(null, tmf.getTrustManagers(), null);
            try (HttpClient client = HttpClient.newBuilder().sslContext(clientContext).build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("この証明書を信頼したクライアント: status=" + response.statusCode() + " body=" + response.body());
            }
            server.stop(0);
        } finally {
            Files.deleteIfExists(keystoreFile);
            Files.deleteIfExists(dir);
        }
    }
}
