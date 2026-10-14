// 検証しない: 複数ファイルの例。collect.sh で java -Xmx1g -cp ... exam.Memory <モード> として実行している
package exam;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

// 履歴のために、操作ごとに全体の写しを持つ方式と、操作（変更前の値だけ）を持つ方式の、GC後のメモリ
public class Memory {
    static long usedMB() {
        for (int i = 0; i < 3; i++) System.gc();
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    public static void main(String[] args) {
        String mode = args[0];
        int students = 1000;
        int operations = 2000;
        ScoreBook book = new ScoreBook();
        for (int i = 0; i < students; i++) book.put(new Student("受験者" + i, i % 101, (i * 7) % 101, (i * 13) % 101));
        long before = usedMB();

        Deque<Object> history = new ArrayDeque<>();
        for (int i = 0; i < operations; i++) {
            String name = "受験者" + (i % students);
            if (mode.equals("snapshot")) {
                history.push(book.snapshot());   // 操作の前に、全体の写しを取る
                book.put(book.get(name).withScore("math", i % 101));
            } else {
                Command command = new SetScore(book, name, "math", i % 101);
                command.execute();
                history.push(command);
            }
        }
        long after = usedMB();
        System.out.printf("%-8s 受験者 %,d 人、操作 %,d 回の履歴を持ったあとのGC後の使用量の増加: %d MB%n", mode, students, operations, after - before);
        java.lang.ref.Reference.reachabilityFence(history);
    }
}
