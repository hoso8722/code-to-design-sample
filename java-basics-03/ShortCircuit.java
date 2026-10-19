// 実行: java ShortCircuit.java
public class ShortCircuit {
    static boolean check(String label, boolean result) {
        System.out.println("  " + label + " を評価した");
        return result;
    }

    public static void main(String[] args) {
        System.out.println("false && ... :");
        boolean r1 = check("左", false) && check("右", true);
        System.out.println("  結果 " + r1);

        System.out.println("true || ... :");
        boolean r2 = check("左", true) || check("右", false);
        System.out.println("  結果 " + r2);

        System.out.println("false & ... (短絡しない):");
        boolean r3 = check("左", false) & check("右", true);
        System.out.println("  結果 " + r3);

        // 短絡評価を使った安全な書き方
        String text = null;
        boolean ok = text != null && text.length() > 0;
        System.out.println("null でも例外にならない: " + ok);
    }
}
