// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 入れ子の深さと、再帰の限界を調べる
public class Depth {
    static Node chain(int depth) {
        Node node = new Leaf("底", 1);
        for (int i = 0; i < depth; i++) node = new Group("g").add(node);
        return node;
    }

    public static void main(String[] args) {
        int[] depths = {1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000};
        for (int depth : depths) {
            Node tree = chain(depth);
            try {
                int total = tree.total();
                System.out.printf("classic 深さ %,d: 合計=%d%n", depth, total);
            } catch (StackOverflowError e) {
                System.out.printf("classic 深さ %,d: StackOverflowError%n", depth);
            }
        }
    }
}
