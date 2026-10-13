// 実行: java BreakContinue.java
void main() {
    // continue: 今回の繰り返しの残りを飛ばして、次へ進む
    IO.print("奇数だけ表示: ");
    for (int i = 1; i <= 10; i++) {
        if (i % 2 == 0) {
            continue;
        }
        IO.print(i + " ");
    }
    IO.println();

    // break: 繰り返しを抜ける
    IO.print("最初の60点未満が見つかるまで表示: ");
    int[] scores = {80, 75, 55, 90};
    for (int score : scores) {
        if (score < 60) {
            IO.println("(" + score + " で中断)");
            break;
        }
        IO.print(score + " ");
    }

    // ラベルつき break: 外側の繰り返しまで抜ける
    int[][] table = {{3, 5}, {8, 9}, {2, 4}};
    search:
    for (int row = 0; row < table.length; row++) {
        for (int col = 0; col < table[row].length; col++) {
            if (table[row][col] == 9) {
                IO.println("9 は " + row + " 行 " + col + " 列にある");
                break search;
            }
        }
    }
}
