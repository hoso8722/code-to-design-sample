package exam;

// 科目に対する処理。switch の網羅性は、コンパイラが確かめる
public class Subjects {

    static int score(Subject subject) {
        return switch (subject) {
            case Required s -> s.points();
            case Elective s -> s.points() / 2;
            case Practical s -> s.points() * 8 / 10;
            default -> 0;
        };
    }

    static String describe(Subject subject) {
        return switch (subject) {
            case Required s -> "必須科目「" + s.name() + "」" + s.points() + "点（配点100%）";
            case Elective s -> "選択科目「" + s.name() + "」" + s.points() + "点（配点50%）";
            case Practical s -> "実技科目「" + s.name() + "」" + s.points() + "点（配点80%）";
            default -> "";
        };
    }

    static boolean valid(Subject subject) {
        return switch (subject) {
            case Required s -> s.points() >= 0 && s.points() <= 100;
            case Elective s -> s.points() >= 0 && s.points() <= 100;
            case Practical s -> s.points() >= 0 && s.points() <= 100;
            default -> false;
        };
    }
}
