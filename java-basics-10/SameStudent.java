// 実行: java SameStudent.java
class Student {
    int id;
    String name;

    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    boolean isSameStudent(Student other) {
        return other != null && this.id == other.id;
    }
}

public class SameStudent {
    public static void main(String[] args) {
        Student a = new Student(1, "田中");
        Student b = new Student(1, "田中");
        Student c = new Student(2, "鈴木");
        System.out.println("a と b: " + a.isSameStudent(b));
        System.out.println("a と c: " + a.isSameStudent(c));
        System.out.println("a と null: " + a.isSameStudent(null));
    }
}
