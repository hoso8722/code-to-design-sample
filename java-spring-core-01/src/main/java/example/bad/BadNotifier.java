package example.bad;

import example.Mailer;
import example.SmtpMailer;

// よくない例:依存を自分でnewして作る
public class BadNotifier {
    private final Mailer mailer = new SmtpMailer();

    public void notifyLoan(String user, String title) {
        mailer.send(user, "「" + title + "」を貸し出しました");
    }
}
