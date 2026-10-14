package exam;

public class Exam {
    public static boolean judge(ExamType type, Student s) {
        boolean r = type.passes(s);
        System.out.println(s.name() + " (" + type.name().toLowerCase() + ") -> " + (r ? "PASS" : "FAIL"));
        return r;
    }
}
