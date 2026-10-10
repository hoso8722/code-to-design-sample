// 実行: java HostCheck.java
import java.net.URI;
import java.util.Set;

public class HostCheck {
    static final Set<String> ALLOWED_HOSTS = Set.of("api.example.com");

    static boolean looseCheck(String url) {
        return url.startsWith("https://api.example.com");
    }

    static boolean strictCheck(String url) {
        URI uri = URI.create(url);
        return "https".equals(uri.getScheme())
                && uri.getHost() != null
                && ALLOWED_HOSTS.contains(uri.getHost())
                && uri.getUserInfo() == null;
    }

    public static void main(String[] args) {
        String[] urls = {
            "https://api.example.com/v1/items",
            "https://api.example.com.evil.example/v1/items",
            "https://api.example.com@evil.example/v1/items",
            "http://api.example.com/v1/items",
        };
        for (String url : urls) {
            URI uri = URI.create(url);
            System.out.printf("%-50s host=%-30s startsWith=%-5b 検査=%b%n",
                    url, uri.getHost(), looseCheck(url), strictCheck(url));
        }
    }
}
