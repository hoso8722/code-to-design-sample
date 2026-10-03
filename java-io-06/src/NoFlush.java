// 実行: java NoFlush.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class NoFlush {
    public static void main(String[] args) throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            // 1行を受け取ったら、1行を返すサーバー
            Thread serverThread = new Thread(() -> {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     BufferedWriter out = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
                    String line = in.readLine();
                    out.write("受け取りました: " + line + "\n");
                    out.flush();
                } catch (IOException e) {
                    System.out.println("[サーバー] " + e);
                }
            });
            serverThread.start();

            try (Socket socket = new Socket("localhost", server.getLocalPort());
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
                socket.setSoTimeout(500);
                out.write("Aさん,合格\n");  // バッファに入っただけで、まだ送られていない
                try {
                    System.out.println("flush の前: " + in.readLine());
                } catch (SocketTimeoutException e) {
                    System.out.println("flush の前: 500ミリ秒待っても、応答がありません");
                }
                out.flush();
                System.out.println("flush のあと: " + in.readLine());
            }
            serverThread.join();
        }
    }
}
