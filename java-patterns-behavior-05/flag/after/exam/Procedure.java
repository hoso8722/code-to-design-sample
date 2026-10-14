package exam;

// フラグ（文字列）と条件分岐で、状態を表す
public class Procedure {
    private String status = "APPLIED";
    private int score;

    public void take() {
        if (!status.equals("APPLIED")) throw new IllegalStateException(status + " では受験できません");
        status = "TAKEN";
    }

    public void grade(int score) {
        if (!status.equals("TAKEN")) throw new IllegalStateException(status + " では採点できません");
        this.score = score;
        status = "GRADING";
    }

    public void announce() {
        if (!status.equals("GRADING")) throw new IllegalStateException(status + " では発表できません");
        status = "ANNOUNCED";
    }

    public void cancel() {
        if (!status.equals("APPLIED") && !status.equals("TAKEN")) throw new IllegalStateException(status + " では取り消せません");
        status = "CANCELLED";
    }

    public String name() { return status; }

    public String label() {
        return switch (status) {
            case "APPLIED" -> "申込済み";
            case "TAKEN" -> "受験済み";
            case "GRADING" -> "採点中";
            case "ANNOUNCED" -> "結果発表済み（" + score + "点）";
            case "CANCELLED" -> "取り消し済み";
            default -> throw new IllegalStateException(status);
        };
    }
}
