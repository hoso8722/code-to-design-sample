// 実行: java ToStringOverride.java
public class ToStringOverride {
    public static void main(String[] args) {
        Student student = new Student("田中", 75);
        System.out.println(student);             // println が toString を呼ぶ
        System.out.println("学生: " + student);   // 文字列連結でも toString が呼ばれる
    }
}

class Student {
    String name;
    int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        return name + "(" + score + "点)";
    }
}
