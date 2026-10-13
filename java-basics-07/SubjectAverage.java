// 実行: java SubjectAverage.java
void main() {
    String[] subjects = {"国語", "数学", "英語"};
    int[][] scores = {
        {80, 65, 90},
        {70, 85, 60},
        {95, 90, 100}
    };

    for (int col = 0; col < subjects.length; col++) {
        int total = 0;
        for (int row = 0; row < scores.length; row++) {
            total += scores[row][col];
        }
        double average = (double) total / scores.length;
        IO.println(subjects[col] + "の平均: " + String.format("%.1f", average));
    }
}
