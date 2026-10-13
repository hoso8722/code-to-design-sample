// 実行: java IfElse.java
String grade(int score) {
    if (score >= 90) {
        return "A";
    } else if (score >= 80) {
        return "B";
    } else if (score >= 60) {
        return "C";
    } else {
        return "D";
    }
}

void main() {
    int[] scores = {95, 90, 89, 80, 79, 60, 59, 0};
    for (int score : scores) {
        IO.println(score + "点 → " + grade(score));
    }
}
