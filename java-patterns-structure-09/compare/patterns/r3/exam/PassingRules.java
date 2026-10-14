package exam;

public final class PassingRules {
    private PassingRules() {}

    public static PassingRule of(String type) {
        return switch (type) {
            case "general" -> new GeneralRule();
            case "scholarship" -> new ScholarshipRule();
            case "transfer" -> new TransferRule();
            default -> throw new IllegalArgumentException("unknown type: " + type);
        };
    }
}
