package exam;

public class Main {
    public static void main(String[] args) {
        Student student = new Student("伊藤", 60, 60, 0);

        // 別々に選ぶと、区分の違う判定ルールと成績表を組み合わせられてしまう
        PassingRule rule = ExamKit.TRANSFER.rule();
        Scorecard scorecard = ExamKit.GENERAL.scorecard();
        System.out.println("別々に選んだ組: " + rule.passes(student) + " / " + scorecard.format(student));

        // キットから取り出すと、必ず同じ区分の組になる
        ExamKit kit = ExamKit.TRANSFER;
        System.out.println("キットから取り出した組: " + kit.rule().passes(student) + " / " + kit.scorecard().format(student));
    }
}
