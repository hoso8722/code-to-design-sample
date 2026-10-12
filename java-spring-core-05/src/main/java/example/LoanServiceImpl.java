package example;

import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {
    @Override
    public String lend(String user, String title) {
        sleep(30);
        return user + " が「" + title + "」を借りた";
    }

    @Override
    public String giveBack(String user, String title) {
        sleep(10);
        return user + " が「" + title + "」を返した";
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
