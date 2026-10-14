// 実行: mvn test（このフォルダの Step0〜Step6 は、StepsTest から呼ばれる）
package exam;

// 手順0：整理する前（第1回の ExamJudge と同じ）
public class Step0 {
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
