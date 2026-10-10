// 実行: java ConnectTimeout.java
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ConnectTimeout {
    public static void main(String[] args) throws Exception {
        // 待ち行列(backlog)が1つで、acceptしないサーバー。待ち行列を埋めると、次の接続は待たされる
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            int port = server.getLocalPort();
            List<Socket> fillers = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Socket s = new Socket();
                try {
                    s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 300);
                    fillers.add(s);
                } catch (java.net.SocketTimeoutException e) {
                    s.close();
                    break;
                }
            }
            System.out.println("待ち行列を埋めるために開いた接続: " + fillers.size() + " 本");

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/")).build();
            long start = System.nanoTime();
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (HttpConnectTimeoutException e) {
                long ms = (System.nanoTime() - start) / 1_000_000;
                System.out.println("例外: " + e.getClass().getSimpleName() + " \"" + e.getMessage() + "\" (約" + ms + " ms)");
                System.out.println("HttpTimeoutExceptionの一種か: " + (e instanceof HttpTimeoutException));
                System.out.println("IOExceptionの一種か: " + (e instanceof java.io.IOException));
            } finally {
                client.close();
                for (Socket s : fillers) {
                    s.close();
                }
            }
        }
    }
}
