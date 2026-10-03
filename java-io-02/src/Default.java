// 実行: java Default.java
import java.nio.charset.Charset;

public class Default {
    public static void main(String[] args) {
        System.out.println("既定の文字コード: " + Charset.defaultCharset());
        System.out.println("file.encoding: " + System.getProperty("file.encoding"));
        System.out.println("native.encoding: " + System.getProperty("native.encoding"));
        System.out.println("stdout.encoding: " + System.getProperty("stdout.encoding"));
    }
}
