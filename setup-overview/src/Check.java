// 実行: java Check.java
import java.nio.charset.Charset;

// 記事のコードを動かす前に、使われるJDKと設定を確かめる
public class Check {
    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Javaのバージョン: " + Runtime.version());
        System.out.println("JDKの場所（java.home）: " + System.getProperty("java.home"));
        System.out.println("提供元: " + System.getProperty("java.vendor"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("既定の文字コード: " + Charset.defaultCharset());
        System.out.println("使えるCPUの数: " + runtime.availableProcessors());
        System.out.println("ヒープの上限: " + runtime.maxMemory() / (1024 * 1024) + " MB");
    }
}
