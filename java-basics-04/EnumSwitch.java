// 実行: java EnumSwitch.java
enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

boolean isHoliday(Day day) {
    return switch (day) {
        case SAT, SUN -> true;
        case MON, TUE, WED, THU, FRI -> false;   // すべての値を書くと、default は要らない
    };
}

void main() {
    for (Day day : Day.values()) {
        IO.println(day + " : " + (isHoliday(day) ? "休み" : "平日"));
    }
}
