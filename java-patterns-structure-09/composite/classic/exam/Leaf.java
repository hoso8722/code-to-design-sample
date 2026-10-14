package exam;

public class Leaf implements Node {
    private final String name;
    private final int points;

    public Leaf(String name, int points) {
        this.name = name;
        this.points = points;
    }

    @Override public int total() { return points; }
    @Override public String describe() { return name + "=" + points; }
}
