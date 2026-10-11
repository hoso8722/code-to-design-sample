// 実行: mvn -q compile exec:java -Dexec.mainClass=example.Main
package example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("ログを書く");
        System.out.println("SLF4Jの実装: " + LoggerFactory.getILoggerFactory().getClass().getName());
    }
}
