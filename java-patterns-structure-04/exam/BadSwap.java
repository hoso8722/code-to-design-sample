// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class BadSwap {
    public static void main(String[] args) {
        // Bonus と Preference を入れ替えた呼び出し
        new TypedApplication("佐藤", new Scores(60, 90, 50),
                new TypedApplication.Preference(1), new TypedApplication.Bonus(10));
    }
}
