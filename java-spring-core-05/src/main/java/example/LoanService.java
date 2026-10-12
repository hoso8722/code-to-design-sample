package example;

// 貸出の業務。interface を持つ
public interface LoanService {
    String lend(String user, String title);

    String giveBack(String user, String title);
}
