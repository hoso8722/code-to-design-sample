package exam;

// 成績表の書式。入試区分によって、表示する科目が違う
public interface Scorecard {
    String format(Student student);
}
