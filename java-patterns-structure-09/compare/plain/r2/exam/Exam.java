package exam;

public class Exam {
    public static boolean judge(String type, Student s) {
        boolean r;
        if (type.equals("general")) r = s.total() >= 180;
        else if (type.equals("scholarship")) r = s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60;
        else if (type.equals("transfer")) r = s.math() + s.english() >= 120;
        else if (type.equals("recommendation")) r = s.total() >= 150 && s.english() >= 70;
        else throw new IllegalArgumentException("unknown type: " + type);
        System.out.println(s.name() + " (" + type + ") -> " + (r ? "PASS" : "FAIL"));
        return r;
    }
}
