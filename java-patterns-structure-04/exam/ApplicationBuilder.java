package exam;

// Builder：名前を付けて設定し、build() でまとめて検証する
public class ApplicationBuilder {
    private String name;
    private Integer math, english, science;
    private int bonus = 0;           // 省略時は0
    private int preference = 1;      // 省略時は第一志望
    private boolean scholarship = false;
    private boolean transfer = false;

    public ApplicationBuilder name(String name) { this.name = name; return this; }
    public ApplicationBuilder math(int v) { this.math = v; return this; }
    public ApplicationBuilder english(int v) { this.english = v; return this; }
    public ApplicationBuilder science(int v) { this.science = v; return this; }
    public ApplicationBuilder bonus(int v) { this.bonus = v; return this; }
    public ApplicationBuilder preference(int v) { this.preference = v; return this; }
    public ApplicationBuilder scholarship(boolean v) { this.scholarship = v; return this; }
    public ApplicationBuilder transfer(boolean v) { this.transfer = v; return this; }

    public Application build() {
        if (name == null) throw new IllegalStateException("name が設定されていません");
        if (math == null || english == null || science == null) {
            throw new IllegalStateException("3科目の点数がそろっていません");
        }
        return new Application(name, math, english, science, bonus, preference, scholarship, transfer);
    }
}
