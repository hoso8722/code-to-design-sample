// 実行: java Timeout.java
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Timeout {
    public static void main(String[] args) throws Exception {
        // 1. 接続は受け付けるが、何も返さないサーバー
        try (ServerSocket server = new ServerSocket(0)) {
            Thread serverThread = new Thread(() -> {
                try (Socket socket = server.accept()) {
                    Thread.sleep(3_000);  // 3秒間、何も送らない
                } catch (IOException | InterruptedException e) {
                    System.out.println("[サーバー] " + e);
                }
            });
            serverThread.start();

            try (Socket socket = new Socket("localhost", server.getLocalPort())) {
                socket.setSoTimeout(500);  // read が500ミリ秒待っても何も届かなければ、例外にする
                long start = System.nanoTime();
                try {
                    socket.getInputStream().read();
                } catch (SocketTimeoutException e) {
                    System.out.printf("read: %d ms 待って %s%n",
                        (System.nanoTime() - start) / 1_000_000, e);
                }
            }
            serverThread.join();
        }

        // 2. 誰も待ち受けていないポートに接続する
        int closedPort;
        try (ServerSocket temporary = new ServerSocket(0)) {
            closedPort = temporary.getLocalPort();  // 閉じたあとは、誰も待ち受けていない
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", closedPort), 1_000);  // 接続のタイムアウトは1秒
        } catch (ConnectException e) {
            System.out.println("connect: " + e);
        }
    }
}
