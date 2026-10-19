// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class NoDefaultConstructor {
    public static void main(String[] args) {
        Student s = new Student();
        System.out.println(s.name);
    }
}

class Student {
    String name;

    Student(String name) {
        this.name = name;
    }
}
