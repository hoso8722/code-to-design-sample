// 実行: java Ternary.java
public class Ternary {
    public static void main(String[] args) {
        int score = 72;
        String result = score >= 60 ? "合格" : "不合格";
        System.out.println(result);

        // 三項演算子の入れ子は読みにくい。if の連続や switch 式を使うほうがよい
        String grade = score >= 90 ? "A" : score >= 80 ? "B" : score >= 60 ? "C" : "D";
        System.out.println(grade);
    }
}
