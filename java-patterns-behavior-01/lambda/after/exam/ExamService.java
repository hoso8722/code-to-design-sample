package exam;

import java.util.HashMap;
import java.util.Map;

public class ExamService {
    private static final Map<String, Rule> RULES = new HashMap<>();

    static {
    RULES.put("general", new Rule(s -> s.total() >= 180, "合計180点以上"));
    RULES.put("scholarship", new Rule(s -> s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60, "平均70点以上、各科目60点以上"));
    RULES.put("transfer", new Rule(s -> s.math() + s.english() >= 120, "数学と英語の合計120点以上"));
    RULES.put("recommendation", new Rule(s -> s.total() >= 150 && s.english() >= 70, "合計150点以上、英語70点以上"));
    }

    private static Rule rule(String type) {
        Rule rule = RULES.get(type);
        if (rule == null) throw new IllegalArgumentException("unknown type: " + type);
        return rule;
    }

    public boolean judge(String type, Student s) { return rule(type).test().test(s); }

    public String explain(String type) { return rule(type).explanation(); }
}
