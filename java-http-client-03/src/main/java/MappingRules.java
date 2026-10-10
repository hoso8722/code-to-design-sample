// 実行: mvn -q compile exec:java -Dexec.mainClass=MappingRules
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingRules {
    record Product(String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LenientProduct(String id, String name) {}

    record Renamed(@JsonProperty("product_id") String id, String name) {}

    record WithStock(String id, int stock) {}

    static void show(String label, ObjectMapper mapper, String json, Class<?> type) {
        try {
            System.out.println(label + " -> " + mapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            System.out.println(label + " -> " + e.getClass().getSimpleName());
        }
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        show("未知のプロパティがある      ", mapper, "{\"id\":\"42\",\"name\":\"キーボード\",\"price\":4800}", Product.class);
        show("未知のプロパティを許す(注釈)", mapper, "{\"id\":\"42\",\"name\":\"キーボード\",\"price\":4800}", LenientProduct.class);

        ObjectMapper lenient = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        show("未知のプロパティを許す(設定)", lenient, "{\"id\":\"42\",\"name\":\"キーボード\",\"price\":4800}", Product.class);

        show("プロパティが欠けている      ", mapper, "{\"id\":\"42\"}", Product.class);
        show("数値のプロパティが欠けている", mapper, "{\"id\":\"42\"}", WithStock.class);
        show("名前が違う(product_id)      ", mapper, "{\"product_id\":\"42\",\"name\":\"キーボード\"}", Product.class);
        show("@JsonPropertyで対応づけ     ", mapper, "{\"product_id\":\"42\",\"name\":\"キーボード\"}", Renamed.class);
        show("型が違う(stockが文字列)     ", mapper, "{\"id\":\"42\",\"stock\":\"たくさん\"}", WithStock.class);
        show("JSONとして不正              ", mapper, "{\"id\":\"42\",", Product.class);
    }
}
