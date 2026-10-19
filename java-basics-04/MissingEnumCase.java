// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class MissingEnumCase {
    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {
        Day day = Day.SAT;
        boolean holiday = switch (day) {
            case SAT, SUN -> true;
            case MON, TUE, WED, THU -> false;
        };
        System.out.println(holiday);
    }
}
