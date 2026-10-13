// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class StaticContext {
    int counter = 0;

    public static void main(String[] args) {
        counter++;
        System.out.println(counter);
    }
}
