package exam;

public enum ExamType {
    GENERAL(new GeneralRule()),
    SCHOLARSHIP(new ScholarshipRule()),
    TRANSFER(new TransferRule()),
    RECOMMENDATION(new RecommendationRule());

    private final PassingRule rule;

    ExamType(PassingRule rule) {
        this.rule = rule;
    }

    public PassingRule rule() {
        return rule;
    }

    public static ExamType from(String text) {
        return valueOf(text.toUpperCase());
    }
}
