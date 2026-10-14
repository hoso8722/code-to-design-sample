package exam;

// 合否判定の処理。長い年月をかけて、条件が足されていった状態を想定している
public class ExamJudge {
    public static int passLine = 180;   // 一般入試の合格点（3科目の合計）
    public static boolean verbose = false;

    public static String judge(String name, int math, int english, int science, String type) {
        if (name == null || name.isEmpty()) {
            return "INVALID";
        }
        if (math < 0 || math > 100 || english < 0 || english > 100 || science < 0 || science > 100) {
            return "INVALID";
        }
        String result;
        if (type.equals("general")) {
            int total = math + english + science;
            if (total >= passLine) {
                result = "PASS";
            } else {
                result = "FAIL";
            }
        } else if (type.equals("scholarship")) {
            int total = math + english + science;
            int average = total / 3;
            if (average >= 70 && math >= 60 && english >= 60 && science >= 60) {
                result = "PASS";
            } else {
                result = "FAIL";
            }
        } else if (type.equals("transfer")) {
            int total = math + english;
            if (total >= 120) {
                result = "PASS";
            } else {
                result = "FAIL";
            }
        } else {
            return "INVALID";
        }
        if (verbose) {
            System.out.println(name + " (" + type + "): " + math + "/" + english + "/" + science + " -> " + result);
        }
        return result;
    }
}
