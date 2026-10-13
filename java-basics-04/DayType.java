// 実行: java DayType.java
String dayType(int day) {
    return switch (day) {
        case 1, 2, 3, 4, 5 -> "平日";
        case 6, 7 -> "休み";
        default -> "不正な値";
    };
}

void main() {
    for (int day = 1; day <= 8; day++) {
        IO.println(day + " → " + dayType(day));
    }
}
