package exam;

import java.util.HashMap;
import java.util.Map;

public class ExamService {
    private static final Map<String, PassingRule> RULES = new HashMap<>();

    static {
    RULES.put("general", new GeneralRule());
    RULES.put("scholarship", new ScholarshipRule());
    RULES.put("transfer", new TransferRule());
    RULES.put("recommendation", new RecommendationRule());
    }

    private static PassingRule rule(String type) {
        PassingRule rule = RULES.get(type);
        if (rule == null) throw new IllegalArgumentException("unknown type: " + type);
        return rule;
    }

    public boolean judge(String type, Student s) { return rule(type).passes(s); }

    public String explain(String type) { return rule(type).explain(); }
}
