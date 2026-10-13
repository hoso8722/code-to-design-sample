// 実行: java EqualsDemo.java
class PlainStudent {
    int id;
    String name;

    PlainStudent(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class StudentWithEquals {
    int id;
    String name;

    StudentWithEquals(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof StudentWithEquals that)) return false;
        return id == that.id;            // 学籍番号が同じなら、同じ学生とみなす
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

public class EqualsDemo {
    public static void main(String[] args) {
        PlainStudent p1 = new PlainStudent(1, "田中");
        PlainStudent p2 = new PlainStudent(1, "田中");
        System.out.println("equals を書いていない: p1.equals(p2) = " + p1.equals(p2));

        StudentWithEquals s1 = new StudentWithEquals(1, "田中");
        StudentWithEquals s2 = new StudentWithEquals(1, "田中(別のオブジェクト)");
        System.out.println("equals を書いた: s1.equals(s2) = " + s1.equals(s2));
        System.out.println("equals を書いても、s1 == s2 は " + (s1 == s2));

        System.out.println("equals に null を渡す: " + s1.equals(null));
    }
}
