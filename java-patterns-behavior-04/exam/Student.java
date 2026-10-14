package exam;

public record Student(String name, int math, int english, int science) {
    public Student withScore(String subject, int points) {
        return switch (subject) {
            case "math" -> new Student(name, points, english, science);
            case "english" -> new Student(name, math, points, science);
            case "science" -> new Student(name, math, english, points);
            default -> throw new IllegalArgumentException("unknown subject: " + subject);
        };
    }

    public int score(String subject) {
        return switch (subject) {
            case "math" -> math;
            case "english" -> english;
            case "science" -> science;
            default -> throw new IllegalArgumentException("unknown subject: " + subject);
        };
    }
}
