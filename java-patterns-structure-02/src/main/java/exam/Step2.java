package exam;

// 手順2：入力の検査を取り出す（動きは変えない）
public class Step2 {
    public static int passLine = 180;
    public static boolean verbose = false;

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final int SCHOLARSHIP_AVERAGE_LINE = 70;
    private static final int SCHOLARSHIP_SUBJECT_LINE = 60;
    private static final int TRANSFER_LINE = 120;

    private static boolean isValidName(String name) {
        return name != null && !name.isEmpty();
    }

    private static boolean isValidScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    public static String judge(String name, int math, int english, int science, String type) {
        if (!isValidName(name)) {
            return "INVALID";
        }
        if (!isValidScore(math) || !isValidScore(english) || !isValidScore(science)) {
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
