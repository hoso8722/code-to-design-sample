package example;

// 通知を送る相手。実際の送信は、実装に任せる
public interface Mailer {
    void send(String to, String message);
}
