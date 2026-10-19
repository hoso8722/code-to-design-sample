// 実行: java PassCondition.java
public class PassCondition {
    public static void main(String[] args) {
        int score = 72;
        int attendance = 4;
        boolean passed = score >= 60 && attendance >= 3;
        System.out.println("合格: " + passed);

        score = 55;
        passed = score >= 60 && attendance >= 3;
        System.out.println("点数55のとき: " + passed);

        score = 80;
        attendance = 2;
        passed = score >= 60 && attendance >= 3;
        System.out.println("出席2回のとき: " + passed);
    }
}
