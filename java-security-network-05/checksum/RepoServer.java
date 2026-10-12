// 検証しない: サーバーを起動したままにするため。使い方は java RepoServer.java repo 8099
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

// ディレクトリの中のファイルを、そのまま返す簡単なHTTPサーバー(Mavenのリポジトリの役)
public class RepoServer {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", Integer.parseInt(args[1])), 0);
        server.createContext("/", exchange -> {
            Path file = root.resolve("." + exchange.getRequestURI().getPath()).normalize();
            boolean exists = file.startsWith(root) && Files.isRegularFile(file);
            System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + " -> " + (exists ? 200 : 404));
            if (!exists) {
                exchange.sendResponseHeaders(404, -1);
            } else {
                byte[] bytes = Files.readAllBytes(file);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
    }
}
