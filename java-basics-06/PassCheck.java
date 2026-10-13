// 実行: java PassCheck.java
boolean isPassed(int score, int attendance) {
    return score >= 60 && attendance >= 3;
}

void main() {
    IO.println("72点・出席4回: " + isPassed(72, 4));
    IO.println("55点・出席4回: " + isPassed(55, 4));
    IO.println("80点・出席2回: " + isPassed(80, 2));
}
