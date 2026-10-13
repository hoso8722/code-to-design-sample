// 実行: java AutoId.java
class Student {
    private static int nextId = 1;      // 次に配る学籍番号。全員で共有する

    private final int id;
    private final String name;

    Student(String name) {
        this.id = nextId++;             // 作るたびに、1ずつ増えた番号をもらう
        this.name = name;
    }

    String describe() {
        return "No." + id + " " + name;
    }
}

public class AutoId {
    public static void main(String[] args) {
        System.out.println(new Student("田中").describe());
        System.out.println(new Student("鈴木").describe());
        System.out.println(new Student("佐藤").describe());
    }
}
