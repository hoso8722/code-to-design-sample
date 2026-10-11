// 実行: mvn -q compile exec:java -Dexec.mainClass=example.App
package example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        // 宣言したのは jackson-databind だけ。JsonFactory は、推移的に入ってきた jackson-core のクラス
        JsonFactory factory = new JsonFactory();
        System.out.println(factory.getClass().getName());
        System.out.println(new ObjectMapper().writeValueAsString(Map.of("name", "maven")));
    }
}
