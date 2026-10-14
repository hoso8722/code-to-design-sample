// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.ArrayList;
import java.util.List;

// Runnable だけの履歴と、実行・取り消しの組を持つ Command の違い
public class Lambdas {
    public static void main(String[] args) {
        ScoreBook book = new ScoreBook();
        book.put(new Student("佐藤", 60, 70, 80));

        // Runnable だけを溜める：やり直し（再実行）はできるが、取り消す方法がない
        List<Runnable> runnables = new ArrayList<>();
        Runnable change = () -> book.put(book.get("佐藤").withScore("math", 99));
        runnables.add(change);
        runnables.forEach(Runnable::run);
        System.out.println("Runnable で変更したあと: " + book + "（取り消す方法: なし）");

        // 実行と取り消しの組：取り消せる
        ScoreBook book2 = new ScoreBook();
        book2.put(new Student("佐藤", 60, 70, 80));
        Student[] saved = new Student[1];
        Command command = Command.of("SET 佐藤 math 99",
            () -> { saved[0] = book2.get("佐藤"); book2.put(saved[0].withScore("math", 99)); },
            () -> book2.put(saved[0]));
        command.execute();
        System.out.println("組で変更したあと: " + book2);
        command.undo();
        System.out.println("取り消したあと  : " + book2);
    }
}
