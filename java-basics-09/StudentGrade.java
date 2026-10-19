// 実行: java StudentGrade.java
public class StudentGrade {
    public static void main(String[] args) {
        Student[] students = {
            new Student("田中", 95),
            new Student("鈴木", 72),
            new Student("佐藤", 45)
        };
        for (Student student : students) {
            System.out.println(student.name + ": " + student.score + "点 → " + student.grade());
        }
    }
}

class Student {
    String name;
    int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    String grade() {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 60) return "C";
        return "D";
    }
}
