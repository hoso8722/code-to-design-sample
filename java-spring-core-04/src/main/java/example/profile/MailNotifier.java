package example.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class MailNotifier implements Notifier {
    @Override
    public String notifyLoan(String user, String title) {
        return "[メール] " + user + " へ: 「" + title + "」を貸し出しました";
    }
}
