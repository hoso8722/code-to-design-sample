// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class VarTypeChange {
    public static void main(String[] args) {
        var count = 10;
        count = "ten";
        System.out.println(count);
    }
}
