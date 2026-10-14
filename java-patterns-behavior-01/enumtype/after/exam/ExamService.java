package exam;

public class ExamService {
    private static ExamType type(String text) {
        try {
            return ExamType.valueOf(text.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown type: " + text);
        }
    }

    public boolean judge(String type, Student s) { return type(type).passes(s); }

    public String explain(String type) { return type(type).explain(); }
}
