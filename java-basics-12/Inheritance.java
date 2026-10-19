// 実行: java Inheritance.java
public class Inheritance {
    public static void main(String[] args) {
        System.out.println("new Student を実行:");
        Student student = new Student("田中", 75);
        System.out.println(student.introduce());

        // 親の型の変数に、子のオブジェクトを入れられる
        Person person = student;
        System.out.println("親の型の変数から呼んでも、子の introduce が動く:");
        System.out.println(person.introduce());

        Person plain = new Person("鈴木");
        System.out.println(plain.introduce());
    }
}

class Person {
    String name;

    Person(String name) {
        this.name = name;
        System.out.println("  Person のコンストラクタ: " + name);
    }

    String introduce() {
        return "私は" + name + "です";
    }
}

class Student extends Person {          // extends: Person を引き継ぐ
    int score;

    Student(String name, int score) {
        super(name);                    // 親のコンストラクタを先に呼ぶ
        this.score = score;
        System.out.println("  Student のコンストラクタ: " + score);
    }

    @Override                           // 親のメソッドを上書きしていることを明示する
    String introduce() {
        return super.introduce() + "。点数は" + score + "点です";
    }
}
