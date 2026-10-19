// 実行: java RecordExample.java
public class RecordExample {
    public static void main(String[] args) {
        Point p = new Point(3, 4);
        System.out.println(p);                 // toString が自動で作られる
        System.out.println("x = " + p.x() + "、y = " + p.y());   // アクセサも自動

        StudentRecord s = new StudentRecord("田中", 75);
        System.out.println(s + "、合格: " + s.isPassed());

        System.out.println("同じ値の record は equals で等しい: " + new Point(3, 4).equals(p));
    }
}

// 値を持つだけのクラスは、record で短く書ける
record Point(int x, int y) {}

record StudentRecord(String name, int score) {
    boolean isPassed() {
        return score >= 60;
    }
}
