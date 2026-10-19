// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class VarWithoutValue {
    public static void main(String[] args) {
        var count;
        count = 10;
        System.out.println(count);
    }
}
