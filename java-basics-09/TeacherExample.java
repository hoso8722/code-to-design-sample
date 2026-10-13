// 実行: java TeacherExample.java
class Teacher {
    String name;
    String subject;

    Teacher(String name, String subject) {
        this.name = name;
        this.subject = subject;
    }

    String introduce() {
        return subject + "担当の" + name + "です";
    }
}

public class TeacherExample {
    public static void main(String[] args) {
        Teacher teacher = new Teacher("佐藤", "数学");
        System.out.println(teacher.introduce());
    }
}
