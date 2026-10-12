package example;

import org.springframework.stereotype.Service;

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
