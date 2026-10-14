// 検証しない: 複数ファイルの例。collect.sh で java jdk/JdkTemplates.java として実行している
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractList;
import java.util.List;

public class JdkTemplates {
    public static void main(String[] args) throws IOException {
        // 1. InputStream：read() だけを実装すると、read(byte[]) などが、その read() を呼んで動く
        int[] calls = {0};
        InputStream in = new InputStream() {
            int next = 0;
            @Override public int read() {
                calls[0]++;
                return next < 5 ? next++ : -1;
            }
        };
        byte[] buffer = new byte[10];
        int n = in.read(buffer);
        System.out.println("read(byte[]) が返した長さ: " + n + "、子クラスの read() が呼ばれた回数: " + calls[0]);

        // 2. AbstractList：get と size だけを実装すると、contains や toString などが動く
        List<String> list = new AbstractList<>() {
            @Override public String get(int index) { return "科目" + index; }
            @Override public int size() { return 3; }
        };
        System.out.println("toString: " + list + "、contains(\"科目2\"): " + list.contains("科目2"));
    }
}
