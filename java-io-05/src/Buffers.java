// 実行: java Buffers.java
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Buffers {
    static void show(String label, ByteBuffer buffer) {
        System.out.printf("%-12s position=%d limit=%d capacity=%d%n",
            label, buffer.position(), buffer.limit(), buffer.capacity());
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        show("作った直後", buffer);

        buffer.put("PASS".getBytes(StandardCharsets.UTF_8));  // 4バイトを書き込む
        show("put のあと", buffer);

        buffer.flip();  // 書き込みから、読み出しに切り替える
        show("flip のあと", buffer);

        byte[] read = new byte[buffer.remaining()];
        buffer.get(read);  // position から limit までを読み出す
        show("get のあと", buffer);
        System.out.println("読み出した内容: " + new String(read, StandardCharsets.UTF_8));

        buffer.clear();  // もう一度、書き込めるようにする
        show("clear のあと", buffer);
    }
}
