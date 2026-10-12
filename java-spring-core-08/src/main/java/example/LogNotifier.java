package example;

import org.springframework.stereotype.Component;

@Component
public class LogNotifier implements Notifier {
    @Override
    public String notifyLoan(String user, String title) {
        return "[ログ] " + user + " / " + title;
    }
}
