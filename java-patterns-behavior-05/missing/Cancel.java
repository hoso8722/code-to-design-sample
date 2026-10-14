// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 取り消し済みの状態の label() を呼ぶ
public class Cancel {
    public static void main(String[] args) {
        Procedure p = new Procedure();
        p.cancel();
        System.out.println(p.label());
    }
}
