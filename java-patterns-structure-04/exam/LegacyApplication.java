package exam;

// 引数が多いコンストラクタ。同じ型（int）が並び、検証もない
public class LegacyApplication {
    final String name;
    final int math;
    final int english;
    final int science;
    final int bonus;        // 加点（部活動などの評価）
    final int preference;   // 志望順位（1が第一志望）
    final boolean scholarship;
    final boolean transfer;

    public LegacyApplication(String name, int math, int english, int science, int bonus, int preference,
                             boolean scholarship, boolean transfer) {
        this.name = name;
        this.math = math;
        this.english = english;
        this.science = science;
        this.bonus = bonus;
        this.preference = preference;
        this.scholarship = scholarship;
        this.transfer = transfer;
    }

    // 英語を2倍に数える。第一志望なら加点を全部、そうでなければ半分だけ加える
    public int score() {
        int extra = preference == 1 ? bonus : bonus / 2;
        return math + english * 2 + science + extra;
    }

    public boolean isValid() {
        return name != null && !name.isEmpty()
                && math >= 0 && math <= 100 && english >= 0 && english <= 100 && science >= 0 && science <= 100
                && bonus >= 0 && bonus <= 20 && preference >= 1 && preference <= 3;
    }
}
