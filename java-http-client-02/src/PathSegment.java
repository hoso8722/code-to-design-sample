// 実行: java PathSegment.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PathSegment {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/files/", exchange -> {
            URI uri = exchange.getRequestURI();
            byte[] body = ("rawPath=" + uri.getRawPath() + "  path=" + uri.getPath())
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        String fileName = "my file+v2.txt";
        String formEncoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String pathEncoded = formEncoded.replace("+", "%20");
        System.out.println("URLEncoder の結果        : " + formEncoded);
        System.out.println("空白を %20 に直した結果  : " + pathEncoded);
        System.out.println();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String segment : new String[] {formEncoded, pathEncoded}) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/files/" + segment)).build();
                System.out.println("送った /files/" + segment);
                System.out.println("  " + client.send(request, HttpResponse.BodyHandlers.ofString()).body());
            }
        }
        server.stop(0);
    }
}
