// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.LookupDemo
package example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupDemo {
    private static final Logger log = LogManager.getLogger(LookupDemo.class);

    public static void main(String[] args) {
        // 利用者の入力に見立てた文字列。${java:version} は、Log4jの置換機能(lookup)の書式
        String input = "${java:version}";
        log.error("利用者の入力: {}", input);
    }
}
