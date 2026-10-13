// 実行: java ShortCircuit.java
boolean check(String label, boolean result) {
    IO.println("  " + label + " を評価した");
    return result;
}

void main() {
    IO.println("false && ... :");
    boolean r1 = check("左", false) && check("右", true);
    IO.println("  結果 " + r1);

    IO.println("true || ... :");
    boolean r2 = check("左", true) || check("右", false);
    IO.println("  結果 " + r2);

    IO.println("false & ... (短絡しない):");
    boolean r3 = check("左", false) & check("右", true);
    IO.println("  結果 " + r3);

    // 短絡評価を使った安全な書き方
    String text = null;
    boolean ok = text != null && text.length() > 0;
    IO.println("null でも例外にならない: " + ok);
}
