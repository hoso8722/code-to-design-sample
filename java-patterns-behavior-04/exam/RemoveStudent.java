package exam;

public class RemoveStudent implements Command {
    private final ScoreBook book;
    private final String name;
    private Student removed;

    public RemoveStudent(ScoreBook book, String name) {
        this.book = book;
        this.name = name;
    }

    @Override public void execute() { removed = book.remove(name); }
    @Override public void undo() { book.put(removed); }
    @Override public String describe() { return "REMOVE " + name; }
}
