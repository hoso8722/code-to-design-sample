package exam;

// record：検証をコンパイル時に近い場所（コンストラクタ）に置く。引数の取り違えは防げない
public record Application(String name, int math, int english, int science, int bonus, int preference,
                          boolean scholarship, boolean transfer) {
    public Application {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name");
        check("math", math, 0, 100);
        check("english", english, 0, 100);
        check("science", science, 0, 100);
        check("bonus", bonus, 0, 20);
        check("preference", preference, 1, 3);
    }

    private static void check(String label, int value, int min, int max) {
        if (value < min || value > max) throw new IllegalArgumentException(label + "=" + value);
    }

    // 加点・志望順位・区分に、デフォルトの値を使う作り方
    public static Application basic(String name, int math, int english, int science) {
        return new Application(name, math, english, science, 0, 1, false, false);
    }

    public int score() {
        int extra = preference == 1 ? bonus : bonus / 2;
        return math + english * 2 + science + extra;
    }
}
