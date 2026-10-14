package exam;

public class ReportPrinter {
    public String describe(String type) {
        return PassingRules.of(type).describe();
    }
}
