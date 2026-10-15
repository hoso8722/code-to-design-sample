// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 帳簿にいない受験者に、点数の変更を実行したとき
public class MissingTarget {
    public static void main(String[] args) {
        ScoreBook book = new ScoreBook();
        History history = new History();
        history.run(new AddStudent(book, new Student("佐藤", 60, 70, 80)));
        try {
            history.run(new SetScore(book, "存在しない人", "math", 90));
        } catch (NullPointerException e) {
            System.out.println("例外: " + e.getClass().getSimpleName());
        }
        System.out.println("履歴に積まれた件数: " + history.doneCount() + "（追加の1件だけ。失敗した操作は積まれていない）");
    }
}
