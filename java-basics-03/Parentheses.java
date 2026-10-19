// 実行: java Parentheses.java
public class Parentheses {
    public static void main(String[] args) {
        int attendance = 4;
        int score = 40;
        boolean retestPassed = true;

        // 括弧がないと、&& が先に計算される: (出席 >= 3 && 点数 >= 60) || 追試合格
        boolean unclear = attendance >= 3 && score >= 60 || retestPassed;
        // 意図を括弧で書く: 出席が3回以上で、かつ(点数が60以上、または追試に合格)
        boolean clear = attendance >= 3 && (score >= 60 || retestPassed);
        System.out.println("括弧なし: " + unclear);
        System.out.println("括弧あり: " + clear);

        attendance = 1;
        System.out.println("出席1回のとき、括弧なし: " + (attendance >= 3 && score >= 60 || retestPassed));
        System.out.println("出席1回のとき、括弧あり: " + (attendance >= 3 && (score >= 60 || retestPassed)));
    }
}
