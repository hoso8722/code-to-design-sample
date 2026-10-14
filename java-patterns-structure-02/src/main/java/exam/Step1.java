package exam;

// 手順1：数字に名前を付ける（動きは変えない）
public class Step1 {
    public static int passLine = 180;   // 一般入試の合格点（3科目の合計）
    public static boolean verbose = false;

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final int SCHOLARSHIP_AVERAGE_LINE = 70;
    private static final int SCHOLARSHIP_SUBJECT_LINE = 60;
    private static final int TRANSFER_LINE = 120;

    public static String judge(String name, int math, int english, int science, String type) {
        if (name == null || name.isEmpty()) {
            return "INVALID";
        }
        if (math < MIN_SCORE || math > MAX_SCORE || english < MIN_SCORE || english > MAX_SCORE
                || science < MIN_SCORE || science > MAX_SCORE) {
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
            if (average >= SCHOLARSHIP_AVERAGE_LINE && math >= SCHOLARSHIP_SUBJECT_LINE
                    && english >= SCHOLARSHIP_SUBJECT_LINE && science >= SCHOLARSHIP_SUBJECT_LINE) {
                result = "PASS";
            } else {
                result = "FAIL";
            }
        } else if (type.equals("transfer")) {
            int total = math + english;
            if (total >= TRANSFER_LINE) {
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
