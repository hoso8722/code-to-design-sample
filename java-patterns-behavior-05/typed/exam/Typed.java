package exam;

// 状態ごとに別の型にして、許される操作だけをメソッドとして持たせる
public class Typed {
    public record Applied() {
        public Taken take() { return new Taken(); }
        public Cancelled cancel() { return new Cancelled(); }
    }

    public record Taken() {
        public Grading grade(int score) { return new Grading(score); }
        public Cancelled cancel() { return new Cancelled(); }
    }

    public record Grading(int score) {
        public Announced announce() { return new Announced(score); }
    }

    public record Announced(int score) {}

    public record Cancelled() {}
}
