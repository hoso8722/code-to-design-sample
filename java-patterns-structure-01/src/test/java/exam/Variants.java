package exam;

// 「整理したつもり」で入りやすい変更。ExamJudge.judge と同じ引数で呼べる
public class Variants {
    // 正しく整理した版（合計を求める処理だけを取り出した）
    static int sum(int a, int b, int c) { return a + b + c; }

    static String extracted(String name, int m, int e, int s, String type) {
        if (name == null || name.isEmpty()) return "INVALID";
        if (m < 0 || m > 100 || e < 0 || e > 100 || s < 0 || s > 100) return "INVALID";
        return switch (type) {
            case "general" -> sum(m, e, s) >= ExamJudge.passLine ? "PASS" : "FAIL";
            case "scholarship" -> sum(m, e, s) / 3 >= 70 && m >= 60 && e >= 60 && s >= 60 ? "PASS" : "FAIL";
            case "transfer" -> m + e >= 120 ? "PASS" : "FAIL";
            default -> "INVALID";
        };
    }

    // 平均を四捨五入にした
    static String rounded(String name, int m, int e, int s, String type) {
        if (type.equals("scholarship") && name != null && !name.isEmpty() && inRange(m, e, s)) {
            long average = Math.round(sum(m, e, s) / 3.0);
            return average >= 70 && m >= 60 && e >= 60 && s >= 60 ? "PASS" : "FAIL";
        }
        return extracted(name, m, e, s, type);
    }

    // 合格点の比較を >= から > にした
    static String greaterThan(String name, int m, int e, int s, String type) {
        if (type.equals("general") && name != null && !name.isEmpty() && inRange(m, e, s)) {
            return sum(m, e, s) > ExamJudge.passLine ? "PASS" : "FAIL";
        }
        return extracted(name, m, e, s, type);
    }

    // 重複をまとめるとき、編入にも3科目の合計を使った
    static String transferAllSubjects(String name, int m, int e, int s, String type) {
        if (type.equals("transfer") && name != null && !name.isEmpty() && inRange(m, e, s)) {
            return sum(m, e, s) >= 120 ? "PASS" : "FAIL";
        }
        return extracted(name, m, e, s, type);
    }

    // 名前の検査を isBlank() に書き換えた（nullが来ると例外になる）
    static String blankCheck(String name, int m, int e, int s, String type) {
        if (name.isBlank()) return "INVALID";
        return extracted(name, m, e, s, type);
    }

    // 奨学の「理科が60以上」の条件が抜けた
    static String missingScience(String name, int m, int e, int s, String type) {
        if (type.equals("scholarship") && name != null && !name.isEmpty() && inRange(m, e, s)) {
            return sum(m, e, s) / 3 >= 70 && m >= 60 && e >= 60 ? "PASS" : "FAIL";
        }
        return extracted(name, m, e, s, type);
    }

    // 3つの間違い（四捨五入、>、理科の条件の抜け）を、1回の大きな書き換えで同時に入れた
    static String bigRewrite(String name, int m, int e, int s, String type) {
        if (name == null || name.isEmpty() || !inRange(m, e, s)) return "INVALID";
        return switch (type) {
            case "general" -> sum(m, e, s) > ExamJudge.passLine ? "PASS" : "FAIL";
            case "scholarship" -> Math.round(sum(m, e, s) / 3.0) >= 70 && m >= 60 && e >= 60 ? "PASS" : "FAIL";
            case "transfer" -> m + e >= 120 ? "PASS" : "FAIL";
            default -> "INVALID";
        };
    }

    private static boolean inRange(int m, int e, int s) {
        return m >= 0 && m <= 100 && e >= 0 && e <= 100 && s >= 0 && s <= 100;
    }
}
