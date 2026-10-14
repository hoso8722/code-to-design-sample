// 実行: javac -g -d out Average.java && java -cp out Average
public class Average {
    static double average(int[] scores) {
        int sum = 0;
        for (int i = 1; i < scores.length; i++) {
            sum += scores[i];
        }
        return (double) sum / scores.length;
    }

    public static void main(String[] args) {
        int[] scores = {80, 70, 90};
        System.out.println("平均: " + average(scores));
    }
}
