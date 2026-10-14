// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 引数の順序を取り違えたコードが、コンパイルも実行も通る例
public class Mixup {
    public static void main(String[] args) {
        // 正しい呼び出し：数学60、英語90、理科50
        var correct = new LegacyApplication("佐藤", 60, 90, 50, 10, 1, false, false);
        // 取り違えた呼び出し：英語と理科が入れ替わっている（コンパイルは通る）
        var mixed = new LegacyApplication("佐藤", 60, 50, 90, 10, 1, false, false);
        System.out.println("正しい呼び出しの得点  : " + correct.score());
        System.out.println("取り違えた呼び出しの得点: " + mixed.score());

        // 志望順位と加点の取り違え（どちらも int）
        var wrongBonus = new LegacyApplication("鈴木", 70, 70, 70, 1, 10, false, false);
        System.out.println("志望順位と加点を入れ替えた得点: " + wrongBonus.score() + "（有効な入力か: " + wrongBonus.isValid() + "）");

        // Builder：名前を付けて設定するので、取り違えにくい
        var built = new ApplicationBuilder().name("佐藤").math(60).english(90).science(50).bonus(10).build();
        System.out.println("Builder で作った得点    : " + built.score());

        // デフォルトの値を持つ static factory：省略できる項目が少ない場合は、これで足りる
        var basic = Application.basic("佐藤", 60, 90, 50);
        System.out.println("static factory の得点   : " + basic.score());

        // 引数を型でまとめた版：Scores の中の順序は、まだ取り違えられる
        var typed = new TypedApplication("佐藤", new Scores(60, 90, 50),
                new TypedApplication.Bonus(10), new TypedApplication.Preference(1));
        System.out.println("型でまとめた版の得点    : " + typed.score());
        // 次の行は、Bonus と Preference を入れ替えるとコンパイルエラーになる（コメントを外して確かめる）
        // new TypedApplication("佐藤", new Scores(60, 90, 50), new TypedApplication.Preference(1), new TypedApplication.Bonus(10));
    }
}
