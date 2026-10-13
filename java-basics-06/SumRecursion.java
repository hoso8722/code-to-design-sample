// 実行: java SumRecursion.java
int sumTo(int n) {
    if (n == 0) {
        return 0;
    }
    return n + sumTo(n - 1);
}

void main() {
    IO.println("1 から 10 の合計: " + sumTo(10));
    IO.println("1 から 100 の合計: " + sumTo(100));
}
