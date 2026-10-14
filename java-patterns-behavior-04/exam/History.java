package exam;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// 実行した操作の履歴。取り消しと再実行ができる
public class History {
    private final Deque<Command> done = new ArrayDeque<>();
    private final Deque<Command> undone = new ArrayDeque<>();

    public void run(Command command) {
        command.execute();
        done.push(command);
        undone.clear();   // 新しい操作をしたら、再実行できる履歴は捨てる
    }

    public boolean undo() {
        if (done.isEmpty()) return false;
        Command command = done.pop();
        command.undo();
        undone.push(command);
        return true;
    }

    public boolean redo() {
        if (undone.isEmpty()) return false;
        Command command = undone.pop();
        command.execute();
        done.push(command);
        return true;
    }

    public int doneCount() { return done.size(); }
    public int undoneCount() { return undone.size(); }
    // 今の状態を作っている操作の記録（実行した順）。取り消した操作は含まない
    public List<String> effectiveLog() {
        List<String> result = new ArrayList<>();
        for (Command command : done) result.add(command.describe());
        java.util.Collections.reverse(result);
        return result;
    }
}
