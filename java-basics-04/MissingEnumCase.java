// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

void main() {
    Day day = Day.SAT;
    boolean holiday = switch (day) {
        case SAT, SUN -> true;
        case MON, TUE, WED, THU -> false;
    };
    IO.println(holiday);
}
