package example;

// コンストラクタ注入:依存はfinalにでき、渡さないとnewできない
public class CtorNotifier {
    private final Mailer mailer;

    public CtorNotifier(Mailer mailer) {
        this.mailer = mailer;
    }

    public String notifyLoan(String user) {
        return mailer.send(user, "貸し出しました");
    }
}
