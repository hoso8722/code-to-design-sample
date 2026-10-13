// 実行: java PairDemo.java
class Pair<A, B> {
    private final A first;
    private final B second;

    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    A first() { return first; }
    B second() { return second; }
}

public class PairDemo {
    public static void main(String[] args) {
        Pair<String, Integer> student = new Pair<>("田中", 75);
        String name = student.first();
        int score = student.second();
        System.out.println(name + ": " + score + "点");

        Pair<String, String> subject = new Pair<>("数学", "A");
        System.out.println(subject.first() + " の成績は " + subject.second());
    }
}
