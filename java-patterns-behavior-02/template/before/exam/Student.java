package exam;

public record Student(String name, int math, int english, int science) {
    public int total() { return math + english + science; }
}
