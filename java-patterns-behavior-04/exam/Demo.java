// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.Map;

public class Demo {
    public static void main(String[] args) {
        ScoreBook book = new ScoreBook();
        History history = new History();
        Map<String, Student> initial = book.snapshot();

        history.run(new AddStudent(book, new Student("佐藤", 60, 70, 80)));
        history.run(new AddStudent(book, new Student("鈴木", 90, 50, 40)));
        history.run(new SetScore(book, "佐藤", "math", 75));
        history.run(new SetScore(book, "鈴木", "english", 65));
        history.run(new RemoveStudent(book, "鈴木"));
        history.run(new SetScore(book, "佐藤", "science", 85));
        Map<String, Student> finalState = book.snapshot();
        System.out.println("6回の操作のあと: " + book);

        int undone = 0;
        while (history.undo()) undone++;
        System.out.println(undone + "回取り消したあと: " + book + "（最初の状態と同じか: " + book.snapshot().equals(initial) + "）");

        int redone = 0;
        while (history.redo()) redone++;
        System.out.println(redone + "回やり直したあと: " + book + "（6回の操作のあとの状態と同じか: " + book.snapshot().equals(finalState) + "）");

        // 途中まで取り消して、新しい操作をすると、やり直せる履歴は捨てられる
        history.undo();
        history.undo();
        System.out.println("2回取り消した直後: 実行済み " + history.doneCount() + " 件、やり直せる " + history.undoneCount() + " 件");
        history.run(new SetScore(book, "佐藤", "english", 99));
        System.out.println("新しい操作のあと: 実行済み " + history.doneCount() + " 件、やり直せる " + history.undoneCount() + " 件");

        // 操作の記録を作り直して実行すると、同じ状態になる
        ScoreBook replayed = new ScoreBook();
        Replay.apply(replayed, history.effectiveLog());
        System.out.println("記録: " + history.effectiveLog());
        System.out.println("記録から再現した状態: " + replayed);
    }
}
