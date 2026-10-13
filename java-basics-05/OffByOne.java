// 実行: java OffByOne.java
void main() {
    int[] scores = {80, 65, 90};

    int times1 = 0;
    for (int i = 0; i < scores.length; i++) {
        times1++;
    }
    IO.println("i < scores.length の繰り返し回数: " + times1);

    int times2 = 0;
    for (int i = 0; i <= scores.length; i++) {
        times2++;
    }
    IO.println("i <= scores.length の繰り返し回数: " + times2);

    // 「1から10まで」を数える範囲の違い
    int c1 = 0, c2 = 0, c3 = 0;
    for (int i = 1; i <= 10; i++) c1++;
    for (int i = 1; i < 10; i++) c2++;
    for (int i = 0; i < 10; i++) c3++;
    IO.println("1 <= i <= 10 : " + c1 + " 回、1 <= i < 10 : " + c2 + " 回、0 <= i < 10 : " + c3 + " 回");
}
