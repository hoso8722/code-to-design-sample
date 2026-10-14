package exam;

public class ExamService {
    public boolean judge(String type, Student s) {
        if (type.equals("general")) return s.total() >= 180;
    else if (type.equals("scholarship")) return s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60;
    else if (type.equals("transfer")) return s.math() + s.english() >= 120;
    else if (type.equals("recommendation")) return s.total() >= 150 && s.english() >= 70;
    throw new IllegalArgumentException("unknown type: " + type);
    }

    public String explain(String type) {
        if (type.equals("general")) return "合計180点以上";
    else if (type.equals("scholarship")) return "平均70点以上、各科目60点以上";
    else if (type.equals("transfer")) return "数学と英語の合計120点以上";
    else if (type.equals("recommendation")) return "合計150点以上、英語70点以上";
    throw new IllegalArgumentException("unknown type: " + type);
    }
}
