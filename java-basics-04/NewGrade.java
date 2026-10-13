// 実行: java NewGrade.java
String grade(int rank) {
    return switch (rank) {
        case 1 -> "金";
        case 2 -> "銀";
        case 3 -> "銅";
        default -> "入賞なし";
    };
}

void main() {
    for (int rank = 1; rank <= 4; rank++) {
        IO.println(rank + "位 → " + grade(rank));
    }
}
