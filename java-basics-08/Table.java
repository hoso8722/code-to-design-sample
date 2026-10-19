// 実行: java Table.java
public class Table {
    public static void main(String[] args) {
        String[] names = {"田中", "鈴木", "佐藤"};
        int[] scores = {75, 8, 100};
        double[] rates = {0.75, 0.08, 1.0};

        System.out.println(String.format("%-4s|%5s|%7s", "名前", "点数", "割合"));
        System.out.println("-".repeat(20));
        for (int i = 0; i < names.length; i++) {
            System.out.println(String.format("%-4s|%5d|%6.1f%%", names[i], scores[i], rates[i] * 100));
        }
    }
}
