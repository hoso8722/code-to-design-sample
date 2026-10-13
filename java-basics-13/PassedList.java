// 実行: java PassedList.java
import java.util.ArrayList;
import java.util.List;

public class PassedList {
    record Student(String name, int score) {}

    public static void main(String[] args) {
        List<Student> students = List.of(
            new Student("田中", 75), new Student("鈴木", 55), new Student("佐藤", 95));

        List<String> passed = new ArrayList<>();
        for (Student student : students) {
            if (student.score() >= 60) {
                passed.add(student.name());
            }
        }
        System.out.println("合格者: " + passed);
    }
}
