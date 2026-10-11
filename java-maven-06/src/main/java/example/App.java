// 実行: mvn -q package && java -jar target/app-1.0.jar
package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class App {
    public static void main(String[] args) throws Exception {
        // 依存の jackson-databind を使う
        System.out.println(new ObjectMapper().writeValueAsString(Map.of("app", "maven")));

        // ServiceLoader で、クラスパス上のJDBCドライバーを探す(各ドライバーのjarにある META-INF/services/java.sql.Driver を読む)
        List<String> drivers = new ArrayList<>();
        for (Driver driver : ServiceLoader.load(Driver.class)) {
            drivers.add(driver.getClass().getName());
        }
        System.out.println("JDBCドライバー: " + drivers);
    }
}
