package example;

// 貸出の通知を作る。送る相手(Mailer)は、外から渡してもらう
public class Notifier {
    private final Mailer mailer;

    public Notifier(Mailer mailer) {
        this.mailer = mailer;
    }

    public void notifyLoan(String user, String title) {
        mailer.send(user, "「" + title + "」を貸し出しました");
    }
}
