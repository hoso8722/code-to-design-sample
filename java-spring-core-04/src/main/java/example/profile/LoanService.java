package example.profile;

import org.springframework.stereotype.Service;

// 貸出の業務。Notifier の実装がメールかログかは、知らない
@Service
public class LoanService {
    private final Notifier notifier;

    public LoanService(Notifier notifier) {
        this.notifier = notifier;
    }

    public String lend(String user, String title) {
        return notifier.notifyLoan(user, title);
    }
}
