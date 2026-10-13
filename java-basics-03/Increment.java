// 実行: java Increment.java
void main() {
    int i = 5;
    int a = i++;   // 後置: 先に i の値を使い、そのあと 1 増やす
    IO.println("a = " + a + "、i = " + i);

    int j = 5;
    int b = ++j;   // 前置: 先に 1 増やし、その値を使う
    IO.println("b = " + b + "、j = " + j);

    int k = 5;
    k = k++;       // k++ の値(5)を k に代入するので、増えない
    IO.println("k = k++ の結果: " + k);
}
