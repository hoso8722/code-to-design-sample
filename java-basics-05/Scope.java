// 実行: java Scope.java
public class Scope {
    public static void main(String[] args) {
        int total = 0;                 // ループの外で宣言した変数は、ループの後も使える
        for (int i = 1; i <= 3; i++) { // i は for の中だけで使える
            int square = i * i;        // square も、1回ごとにこの中だけ
            total += square;
        }
        System.out.println("2乗の合計: " + total);
    }
}
