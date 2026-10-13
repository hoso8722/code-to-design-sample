// 実行: java Methods.java
// 引数を2つ受け取り、整数を返すメソッド
int add(int a, int b) {
    return a + b;
}

// 戻り値のないメソッド(void)
void printResult(String label, int value) {
    IO.println(label + ": " + value);
}

// 税込み価格を返す
int priceWithTax(int price) {
    return price + price * 10 / 100;
}

void main() {
    int sum = add(3, 4);
    printResult("3 + 4", sum);
    printResult("1000円の税込み価格", priceWithTax(1000));

    // メソッドの戻り値は、そのまま式の中で使える
    IO.println("add(add(1, 2), add(3, 4)) = " + add(add(1, 2), add(3, 4)));
}
