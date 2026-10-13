// 実行: java PassCondition.java
void main() {
    int score = 72;
    int attendance = 4;
    boolean passed = score >= 60 && attendance >= 3;
    IO.println("合格: " + passed);

    score = 55;
    passed = score >= 60 && attendance >= 3;
    IO.println("点数55のとき: " + passed);

    score = 80;
    attendance = 2;
    passed = score >= 60 && attendance >= 3;
    IO.println("出席2回のとき: " + passed);
}
