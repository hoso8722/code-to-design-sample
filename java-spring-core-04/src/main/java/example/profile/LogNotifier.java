package example.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class LogNotifier implements Notifier {
    @Override
    public String notifyLoan(String user, String title) {
        return "[ログ] " + user + " / " + title;
    }
}
