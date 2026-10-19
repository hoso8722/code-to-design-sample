// 実行: java Increment.java
public class Increment {
    public static void main(String[] args) {
        int i = 5;
        int a = i++;   // 後置: 先に i の値を使い、そのあと 1 増やす
        System.out.println("a = " + a + "、i = " + i);

        int j = 5;
        int b = ++j;   // 前置: 先に 1 増やし、その値を使う
        System.out.println("b = " + b + "、j = " + j);

        int k = 5;
        k = k++;       // k++ の値(5)を k に代入するので、増えない
        System.out.println("k = k++ の結果: " + k);
    }
}
