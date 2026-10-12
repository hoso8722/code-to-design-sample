package example.profile;

// 通知の送り方を表すinterface。使う側は、実装を知らない
public interface Notifier {
    String notifyLoan(String user, String title);
}
