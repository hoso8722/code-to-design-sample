package school.model;

public class Student {
    private final String name;
    private final int score;

    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String name() {
        return name;
    }

    public int score() {
        return score;
    }

    // public がない(パッケージ内だけ)
    String secretNote() {
        return name + " のメモ";
    }
}
