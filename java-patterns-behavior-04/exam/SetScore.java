package exam;

// 点数を変更する。取り消すために、変更前の値を覚えておく
public class SetScore implements Command {
    private final ScoreBook book;
    private final String name;
    private final String subject;
    private final int points;
    private Student before;

    public SetScore(ScoreBook book, String name, String subject, int points) {
        this.book = book;
        this.name = name;
        this.subject = subject;
        this.points = points;
    }

    @Override
    public void execute() {
        before = book.get(name);
        book.put(before.withScore(subject, points));
    }

    @Override
    public void undo() {
        book.put(before);
    }

    @Override
    public String describe() { return "SET " + name + " " + subject + " " + points; }
}
