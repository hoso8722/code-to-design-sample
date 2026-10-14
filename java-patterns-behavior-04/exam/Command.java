package exam;

// 操作を表す。実行と取り消しの2つを持つ
public interface Command {
    void execute();
    void undo();
    String describe();

    // 実行と取り消しを、関数の組で作る軽い書き方
    static Command of(String text, Runnable execute, Runnable undo) {
        return new Command() {
            @Override public void execute() { execute.run(); }
            @Override public void undo() { undo.run(); }
            @Override public String describe() { return text; }
        };
    }
}
