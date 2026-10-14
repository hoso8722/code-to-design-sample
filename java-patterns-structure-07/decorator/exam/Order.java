// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

// ログとキャッシュを重ねる順序で、ログの件数がどう変わるか
public class Order {
    public static void main(String[] args) {
        List<Student> calls = List.of(
            new Student("佐藤", 60, 70, 80), new Student("鈴木", 90, 50, 40), new Student("佐藤", 60, 70, 80),
            new Student("高橋", 70, 70, 50), new Student("佐藤", 60, 70, 80), new Student("鈴木", 90, 50, 40),
            new Student("高橋", 70, 70, 50), new Student("佐藤", 60, 70, 80), new Student("鈴木", 90, 50, 40),
            new Student("佐藤", 60, 70, 80));

        System.out.println("== ログの内側にキャッシュ：new LoggingJudge(new CachingJudge(base))");
        Judge outerLog = new LoggingJudge(new CachingJudge(new ScoreJudge()));
        calls.forEach(outerLog::passes);

        System.out.println("== キャッシュの内側にログ：new CachingJudge(new LoggingJudge(base))");
        Judge innerLog = new CachingJudge(new LoggingJudge(new ScoreJudge()));
        calls.forEach(innerLog::passes);

        // キャッシュのキーに名前だけを使うと、同じ名前で点数の違う受験者に、古い結果を返す
        System.out.println("== キャッシュのキーが名前だけの場合");
        Judge cached = new CachingJudge(new ScoreJudge());
        System.out.println("佐藤(60,70,80): " + cached.passes(new Student("佐藤", 60, 70, 80)));
        System.out.println("佐藤(0,0,0)   : " + cached.passes(new Student("佐藤", 0, 0, 0)) + "（本来は false）");
    }
}
