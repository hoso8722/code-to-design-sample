package exam;

public class Exam {
    public static boolean judge(ExamType type, Student s) {
        boolean r = type.passes(s);
        System.out.println(s.name() + " (" + type.name().toLowerCase() + ") " + s.math() + "/" + s.english() + "/" + s.science() + " -> " + (r ? "PASS" : "FAIL"));
        return r;
    }
}
