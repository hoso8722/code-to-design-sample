// 実行: java MaxMin.java
void main() {
    int[] scores = {80, 65, 90, 72, 55};
    int max = scores[0];
    int min = scores[0];
    for (int score : scores) {
        if (score > max) {
            max = score;
        }
        if (score < min) {
            min = score;
        }
    }
    IO.println("最高点: " + max + "、最低点: " + min);
}
