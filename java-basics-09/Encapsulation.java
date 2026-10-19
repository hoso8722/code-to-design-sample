// 実行: java Encapsulation.java
public class Encapsulation {
    public static void main(String[] args) {
        Account account = new Account(1000);
        account.deposit(500);
        System.out.println("残高: " + account.getBalance());

        try {
            account.deposit(-100);
        } catch (IllegalArgumentException e) {
            System.out.println("例外: " + e.getMessage());
        }
    }
}

class Account {
    private int balance;           // private: クラスの外から直接触れない

    Account(int balance) {
        this.balance = balance;
    }

    int getBalance() {             // getter: 値を読み取るメソッド
        return balance;
    }

    void deposit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("入金額は正の数にしてください: " + amount);
        }
        balance += amount;
    }
}
