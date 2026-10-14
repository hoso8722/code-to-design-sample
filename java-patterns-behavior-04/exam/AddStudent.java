package exam;

public class AddStudent implements Command {
    private final ScoreBook book;
    private final Student student;

    public AddStudent(ScoreBook book, Student student) {
        this.book = book;
        this.student = student;
    }

    @Override public void execute() { book.put(student); }
    @Override public void undo() { book.remove(student.name()); }
    @Override public String describe() {
        return "ADD " + student.name() + " " + student.math() + " " + student.english() + " " + student.science();
    }
}
