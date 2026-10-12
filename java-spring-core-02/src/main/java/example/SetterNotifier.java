package example;

import org.springframework.beans.factory.annotation.Autowired;

// setter注入:newしただけでは、依存が入っていない状態が残る
public class SetterNotifier {
    private Mailer mailer;

    @Autowired
    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public String notifyLoan(String user) {
        return mailer.send(user, "貸し出しました");
    }
}
