// 実行: java PartialRead.java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PartialRead {
    // 相手が接続を閉じるまで読み、1回の read が返したバイト数を記録する
    static List<Integer> readSizes(Socket socket) throws IOException {
        List<Integer> sizes = new ArrayList<>();
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024 * 1024];
        int n;
        while ((n = in.read(buffer)) != -1) {
            sizes.add(n);
        }
        return sizes;
    }

    interface Sender {
        void send(OutputStream out) throws IOException;
    }

    // サーバーが sender の内容を送り、クライアントが読む。read の結果を返す
    static List<Integer> exchange(Sender sender) throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            Thread serverThread = new Thread(() -> {
                try (Socket socket = server.accept()) {
                    sender.send(socket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("[サーバー] " + e);
                }
            });
            serverThread.start();
            try (Socket socket = new Socket("localhost", server.getLocalPort())) {
                List<Integer> sizes = readSizes(socket);
                serverThread.join();
                return sizes;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. 100バイトを、3回に分けて write する
        List<Integer> small = exchange(out -> {
            for (int i = 0; i < 3; i++) {
                out.write(new byte[100]);
            }
        });
        System.out.println("100バイトを3回 write: read は " + small.size() + " 回 " + small);

        // 2. 1,000,000バイトを、1回で write する
        List<Integer> large = exchange(out -> out.write(new byte[1_000_000]));
        int total = large.stream().mapToInt(Integer::intValue).sum();
        System.out.println("1,000,000バイトを1回 write: read は " + large.size() + " 回（合計 " + total + " バイト）");
        System.out.println(large);
    }
}
