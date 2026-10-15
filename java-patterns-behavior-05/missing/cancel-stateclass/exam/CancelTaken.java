// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 受験済みの状態から、取り消そうとする（Taken に cancel を書き忘れた版）
public class CancelTaken {
    public static void main(String[] args) {
        Procedure p = new Procedure();
        p.take();
        p.cancel();
        System.out.println(p.label());
    }
}
