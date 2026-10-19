// 実行: java GradeOverload.java
public class GradeOverload {
    static String grade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 60) return "C";
        return "D";
    }

    // 文字列で受け取る版。整数に変換してから、上のメソッドを呼ぶ
    static String grade(String score) {
        return grade(Integer.parseInt(score));
    }

    public static void main(String[] args) {
        System.out.println(grade(85));
        System.out.println(grade("72"));
        System.out.println(grade("45"));
    }
}
