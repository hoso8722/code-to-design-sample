// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.JsonAndShell
package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class JsonAndShell {
    static String run(String... command) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        p.waitFor();
        return out.strip().replace("\n", " / ");
    }

    public static void main(String[] args) throws Exception {
        String name = "sora\",\"admin\":true,\"x\":\"";

        System.out.println("## JSONを文字列の連結で作る");
        String concatenated = "{\"name\":\"" + name + "\"}";
        System.out.println("  連結:     " + concatenated);
        Map<?, ?> parsed = new ObjectMapper().readValue(concatenated, Map.class);
        System.out.println("  読み戻すと、項目が増えている: " + parsed);
        System.out.println("  Jackson:  " + new ObjectMapper().writeValueAsString(Map.of("name", name)));

        System.out.println("## 外部コマンドに入力を渡す");
        String input = "hello; echo 追加で実行された";
        System.out.println("  シェルの文字列に連結: " + run("sh", "-c", "echo 入力は " + input));
        System.out.println("  引数のリストで渡す:   " + run("echo", "入力は", input));
    }
}
