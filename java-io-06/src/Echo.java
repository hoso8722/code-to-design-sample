// 実行: java Echo.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Echo {
    // 接続を1つ受け付けて、受け取った行に「受け取りました: 」を付けて返す
    static void serve(ServerSocket server) {
        try (Socket socket = server.accept();
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println("受け取りました: " + line);
            }
            System.out.println("[サーバー] 相手が接続を閉じました");
        } catch (IOException e) {
            System.out.println("[サーバー] " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {  // 0 を指定すると、空いているポートが使われる
            int port = server.getLocalPort();
            Thread serverThread = new Thread(() -> serve(server));
            serverThread.start();

            try (Socket socket = new Socket("localhost", port);
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                out.println("Aさん,合格");
                System.out.println("[クライアント] " + in.readLine());
                out.println("Dさん,不合格");
                System.out.println("[クライアント] " + in.readLine());
            }
            serverThread.join();
        }
    }
}
