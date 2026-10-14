// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class Good {
    public static void main(String[] args) {
        Typed.Announced result = new Typed.Applied().take().grade(80).announce();
        System.out.println("手続きの結果: " + result);
    }
}
