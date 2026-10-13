// 実行: java ReferenceAssign.java
class Student {
    String name;
    int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
}

public class ReferenceAssign {
    public static void main(String[] args) {
        // 基本型: 代入は、値のコピー
        int x = 10;
        int y = x;
        y = 99;
        System.out.println("基本型: x = " + x + "、y = " + y);

        // 参照型: 代入は、参照(行き先)のコピー
        Student a = new Student("田中", 75);
        Student b = a;           // a と b は、同じオブジェクトを指す
        b.score = 0;
        System.out.println("参照型: a.score = " + a.score + "、b.score = " + b.score);
        System.out.println("a == b : " + (a == b));

        // 新しいオブジェクトを作れば、別のものになる
        Student c = new Student("田中", 75);
        System.out.println("同じ内容の別のオブジェクト: a == c : " + (a == c));

        // 変数を別のオブジェクトに向け直しても、もう一方は変わらない
        b = new Student("鈴木", 60);
        System.out.println("b を向け直したあと: a.name = " + a.name + "、b.name = " + b.name);
    }
}
