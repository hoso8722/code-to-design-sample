// 実行: java GradeOverload.java
String grade(int score) {
    if (score >= 90) return "A";
    if (score >= 80) return "B";
    if (score >= 60) return "C";
    return "D";
}

// 文字列で受け取る版。整数に変換してから、上のメソッドを呼ぶ
String grade(String score) {
    return grade(Integer.parseInt(score));
}

void main() {
    IO.println(grade(85));
    IO.println(grade("72"));
    IO.println(grade("45"));
}
