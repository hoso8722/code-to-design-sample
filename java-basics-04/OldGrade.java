// 実行: java OldGrade.java
String grade(int rank) {
    String result;
    switch (rank) {
        case 1:
            result = "金";
            break;
        case 2:
            result = "銀";
            break;
        case 3:
            result = "銅";
            break;
        default:
            result = "入賞なし";
            break;
    }
    return result;
}

void main() {
    for (int rank = 1; rank <= 4; rank++) {
        IO.println(rank + "位 → " + grade(rank));
    }
}
