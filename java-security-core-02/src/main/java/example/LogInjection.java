// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.LogInjection
package example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogInjection {
    private static final Logger log = LoggerFactory.getLogger(LogInjection.class);

    // 改行と制御文字を、見える形に置き換える
    static String sanitize(String input) {
        return input.replace("\r", "\\r").replace("\n", "\\n");
    }

    public static void main(String[] args) {
        String normal = "sora";
        // 利用者名の入力に、改行と、偽のログの行を含めた
        String attack = "guest\n2026-10-12 10:00:00.000 INFO  example.Admin - 管理者 admin がログインした";

        log.info("ログイン失敗: user={}", normal);
        log.info("ログイン失敗: user={}", attack);
        log.info("ログイン失敗: user={} (改行を置き換えた)", sanitize(attack));
    }
}
