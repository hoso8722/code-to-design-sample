// 実行: java QueryBuilder.java
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryBuilder {
    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // 名前と値のそれぞれを1回だけエンコードして、「名前=値」を「&」でつなぐ
    static String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static void main(String[] args) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("q", "cafe & tea");
        params.put("lang", "ja");
        params.put("note", "a=b");

        String query = buildQuery(params);
        System.out.println("1回だけ  : " + query);

        // エンコード済みの値を、さらにエンコードしてしまった場合
        String twice = encode(encode("cafe & tea"));
        System.out.println("2回      : q=" + twice);
        System.out.println("1回復号  : " + URLDecoder.decode(twice, StandardCharsets.UTF_8));
    }
}
