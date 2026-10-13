// 実行: java FinalDemo.java
import java.util.ArrayList;
import java.util.List;

public class FinalDemo {
    static final int MAX = 100;

    public static void main(String[] args) {
        final int limit = 10;               // final の変数: 一度だけ代入できる
        System.out.println("limit = " + limit + "、MAX = " + MAX);

        // final は「変数の付け替え」を禁じる。参照先の中身は変えられる
        final int[] scores = {80, 65, 90};
        scores[0] = 99;                      // 中身の変更は OK
        System.out.println("final の配列の中身を変更: " + java.util.Arrays.toString(scores));

        final List<String> names = new ArrayList<>();
        names.add("田中");
        names.add("鈴木");
        System.out.println("final のリストに追加: " + names);

        // 値が決まるのが遅い final も書ける(あとで1回だけ代入)
        final String label;
        if (limit > 5) {
            label = "大きい";
        } else {
            label = "小さい";
        }
        System.out.println("label = " + label);
    }
}
