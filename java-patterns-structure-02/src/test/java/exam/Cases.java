package exam;

import java.util.List;

// 現在の動きを実行して記録した入力と結果（特性テストの表）
public class Cases {
    public record Case(String label, String name, int math, int english, int science, String type, String expected) {}

    public static final List<Case> ALL = List.of(
        new Case("一般・ちょうど合格点", "佐藤", 60, 60, 60, "general", "PASS"),
        new Case("一般・1点足りない", "佐藤", 60, 60, 59, "general", "FAIL"),
        new Case("一般・1科目だけ満点", "佐藤", 100, 0, 0, "general", "FAIL"),
        new Case("奨学・平均がちょうど70", "鈴木", 70, 70, 70, "scholarship", "PASS"),
        new Case("奨学・平均が69.67", "鈴木", 70, 70, 69, "scholarship", "FAIL"),
        new Case("奨学・理科が59", "鈴木", 100, 100, 59, "scholarship", "FAIL"),
        new Case("奨学・平均が60", "鈴木", 60, 60, 60, "scholarship", "FAIL"),
        new Case("編入・数学と英語の合計が120", "高橋", 60, 60, 0, "transfer", "PASS"),
        new Case("編入・数学が足りない", "高橋", 59, 60, 100, "transfer", "FAIL"),
        new Case("名前が空", "", 70, 70, 70, "general", "INVALID"),
        new Case("名前がnull", null, 70, 70, 70, "general", "INVALID"),
        new Case("点数が範囲外（101）", "佐藤", 101, 70, 70, "general", "INVALID"),
        new Case("点数が範囲外（-1）", "佐藤", 70, -1, 70, "general", "INVALID"),
        new Case("未知の入試区分", "佐藤", 70, 70, 70, "other", "INVALID")
    );

    // いつも目で確かめている3件（どの入試区分も、ふつうの点数）
    public static final List<Case> MANUAL = List.of(
        new Case("一般・ふつうの点数", "佐藤", 70, 70, 70, "general", "PASS"),
        new Case("奨学・ふつうの点数", "鈴木", 80, 80, 80, "scholarship", "PASS"),
        new Case("編入・ふつうの点数", "高橋", 70, 70, 0, "transfer", "PASS")
    );
}
