// 実行: java EvenSum.java
void main() {
    int sum = 0;
    for (int i = 2; i <= 100; i += 2) {   // 2 から 100 まで、2 ずつ増やす
        sum += i;
    }
    IO.println("1〜100 の偶数の合計: " + sum);
}
