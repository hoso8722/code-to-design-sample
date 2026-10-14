package exam;

// 科目。処理は、各クラスのメソッドとして書く
public abstract class Subject {
    protected final String name;
    protected final int points;

    protected Subject(String name, int points) {
        this.name = name;
        this.points = points;
    }

    abstract int score();
    abstract String describe();
    abstract boolean valid();
}
