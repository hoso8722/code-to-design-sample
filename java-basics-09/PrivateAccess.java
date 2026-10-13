// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
class Account {
    private int balance = 1000;
}

public class PrivateAccess {
    public static void main(String[] args) {
        Account account = new Account();
        System.out.println(account.balance);
    }
}
