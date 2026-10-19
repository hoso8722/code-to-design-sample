// 実行: java StaticMembers.java
public class StaticMembers {
    public static void main(String[] args) {
        System.out.println("作る前の Student.count = " + Student.count);

        Student a = new Student("田中", 75);
        Student b = new Student("鈴木", 55);
        System.out.println("2人作った後の Student.count = " + Student.count);
        System.out.println("a.count = " + a.count + "、b.count = " + b.count + "(同じ共有の値)");

        System.out.println("Student.isValidScore(105) = " + Student.isValidScore(105));
        System.out.println("合格点: " + Student.PASS_SCORE + "、田中: " + a.isPassed() + "、鈴木: " + b.isPassed());

        // Math.max や Integer.parseInt も、static メソッド
        System.out.println("Math.max(3, 7) = " + Math.max(3, 7));
    }
}

class Student {
    static int count = 0;                   // static フィールド: クラスに1つだけ。全インスタンスで共有される
    static final int PASS_SCORE = 60;       // 定数: static final で書く

    String name;                            // インスタンスフィールド: オブジェクトごとに別々
    int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
        count++;                            // 作られるたびに、共有のカウンターを増やす
    }

    boolean isPassed() {
        return score >= PASS_SCORE;
    }

    // static メソッド: オブジェクトなしで、クラス名から呼べる
    static boolean isValidScore(int score) {
        return score >= 0 && score <= 100;
    }
}
