// 実行: java UriParts.java
import java.net.URI;

public class UriParts {
    public static void main(String[] args) {
        URI uri = URI.create("https://api.example.com:8443/v1/items/42?lang=ja&sort=price#reviews");
        System.out.println("scheme   : " + uri.getScheme());
        System.out.println("host     : " + uri.getHost());
        System.out.println("port     : " + uri.getPort());
        System.out.println("path     : " + uri.getPath());
        System.out.println("query    : " + uri.getQuery());
        System.out.println("fragment : " + uri.getFragment());

        try {
            URI.create("http://localhost/search?q=cafe & tea");
        } catch (IllegalArgumentException e) {
            System.out.println("空白を含むURI -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        URI japanese = URI.create("http://localhost/search?q=日本語");
        System.out.println("日本語のまま  -> getQuery=" + japanese.getQuery() + " / getRawQuery=" + japanese.getRawQuery());
        System.out.println("               toASCIIString=" + japanese.toASCIIString());
    }
}
