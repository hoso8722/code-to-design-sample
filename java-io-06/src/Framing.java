// 実行: java Framing.java
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Framing {
    // メッセージを送る。先に長さ（4バイトの整数）を送り、続けて本体を送る
    static void send(DataOutputStream out, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        out.writeInt(body.length);
        out.write(body);
        out.flush();
    }

    // メッセージを受け取る。長さを読み、その長さの分だけ本体を読む
    static String receive(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] body = new byte[length];
        in.readFully(body);  // length バイトがそろうまで、読み続ける
        return new String(body, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            Thread serverThread = new Thread(() -> {
                try (Socket socket = server.accept();
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                    send(out, "Aさん,合格");
                    send(out, "Dさん,不合格");
                    send(out, "結果".repeat(100_000));  // 600,000バイトの長いメッセージ
                } catch (IOException e) {
                    System.out.println("[サーバー] " + e);
                }
            });
            serverThread.start();

            try (Socket socket = new Socket("localhost", server.getLocalPort());
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {
                System.out.println("1通目: " + receive(in));
                System.out.println("2通目: " + receive(in));
                System.out.println("3通目: " + receive(in).length() + "文字");
            }
            serverThread.join();
        }
    }
}
