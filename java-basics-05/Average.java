// 実行: java Average.java
void main() {
    int[] scores = {80, 65, 90, 72, 55};
    int total = 0;
    for (int score : scores) {
        total += score;
    }
    double average = (double) total / scores.length;
    IO.println("人数: " + scores.length + "、合計: " + total + "、平均: " + average);
}
