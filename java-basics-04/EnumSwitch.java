// 実行: java EnumSwitch.java
public class EnumSwitch {
    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    static boolean isHoliday(Day day) {
        return switch (day) {
            case SAT, SUN -> true;
            case MON, TUE, WED, THU, FRI -> false;   // すべての値を書くと、default は要らない
        };
    }

    public static void main(String[] args) {
        for (Day day : Day.values()) {
            System.out.println(day + " : " + (isHoliday(day) ? "休み" : "平日"));
        }
    }
}
