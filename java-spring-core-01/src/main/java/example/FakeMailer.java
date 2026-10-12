package example;

import java.util.ArrayList;
import java.util.List;

// テスト用の実装。送ったつもりの内容を、リストに溜める
public class FakeMailer implements Mailer {
    public final List<String> outbox = new ArrayList<>();

    @Override
    public void send(String to, String message) {
        outbox.add(to + ": " + message);
    }
}
