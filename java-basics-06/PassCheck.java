// 実行: java PassCheck.java
public class PassCheck {
    static boolean isPassed(int score, int attendance) {
        return score >= 60 && attendance >= 3;
    }

    public static void main(String[] args) {
        System.out.println("72点・出席4回: " + isPassed(72, 4));
        System.out.println("55点・出席4回: " + isPassed(55, 4));
        System.out.println("80点・出席2回: " + isPassed(80, 2));
    }
}
