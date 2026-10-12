// 実行: java ChecklistScan.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class ChecklistScan {
    // シリーズで再現した欠陥の「兆候」を、文字列で探すだけの粗い点検。見つかった行は、人が確かめる
    record Rule(String id, String owasp, Pattern pattern, String advice) {}

    static final List<Rule> RULES = List.of(
            new Rule("#01", "A01", Pattern.compile("new File\\(.*(getParameter|userInput|name)"), "ユーザー入力でパスを組み立てている。normalizeして基準のフォルダの下か確かめる"),
            new Rule("#02", "A05", Pattern.compile("(Runtime\\.getRuntime\\(\\)\\.exec|new ProcessBuilder)\\(.*\"(sh|bash)\"\\s*,\\s*\"-c\""), "シェルに文字列を渡している。引数のリストで渡す"),
            new Rule("#03", "A08", Pattern.compile("new ObjectInputStream\\("), "Javaのデシリアライズ。ObjectInputFilterで許可する型を絞る"),
            new Rule("#03", "A02", Pattern.compile("DocumentBuilderFactory\\.newInstance\\(\\)"), "XMLパーサーの設定を確かめる(外部エンティティの無効化)"),
            new Rule("#04", "A04", Pattern.compile("MessageDigest\\.getInstance\\(\"(MD5|SHA-1|SHA-256)\"\\)"), "パスワードのハッシュなら、速い関数は不向き。bcryptなどを使う"),
            new Rule("#05", "A04", Pattern.compile("new Random\\(|Math\\.random\\("), "トークンや鍵に使うなら、SecureRandomにする"),
            new Rule("#05", "A04", Pattern.compile("(?i)(password|secret|apikey)\\s*=\\s*\"[^\"]+\""), "秘密が、ソースに書かれている。環境変数などから渡す"));

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("scan-sample");
        Files.writeString(dir.resolve("BadExample.java"), """
                import java.io.*;
                import java.security.*;
                import java.util.*;
                import javax.xml.parsers.*;

                class BadExample {
                    static final String password = "abc123";

                    Object load(InputStream in) throws Exception {
                        return new ObjectInputStream(in).readObject();
                    }

                    String token() {
                        return Long.toHexString(new Random().nextLong());
                    }

                    byte[] hash(byte[] pw) throws Exception {
                        return MessageDigest.getInstance("SHA-256").digest(pw);
                    }

                    File open(String name) {
                        return new File("/data/" + name);
                    }

                    void run(String host) throws Exception {
                        Runtime.getRuntime().exec(new String[] {"sh", "-c", "ping " + host});
                    }

                    Object parse() throws Exception {
                        return DocumentBuilderFactory.newInstance();
                    }
                }
                """);

        try (var files = Files.list(dir)) {
            for (Path file : files.toList()) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    for (Rule rule : RULES) {
                        if (rule.pattern().matcher(lines.get(i)).find()) {
                            System.out.printf("%s:%d [%s/%s] %s%n    %s%n",
                                    file.getFileName(), i + 1, rule.id(), rule.owasp(), rule.advice(), lines.get(i).strip());
                        }
                    }
                }
            }
        }
    }
}
