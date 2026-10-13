// 実行: java Builder.java
void main() {
    int n = 20_000;

    for (int round = 0; round < 3; round++) {   // 3回目の値を使う(最初の2回はウォームアップ)
        long t0 = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "x";                           // 毎回、新しい文字列を作る
        }
        long concat = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("x");                     // 同じバッファに追記する
        }
        String result = sb.toString();
        long builder = System.nanoTime() - t1;

        if (round == 2) {
            IO.println("文字数: " + s.length() + "、" + result.length());
            IO.println("+ で連結: " + concat / 1_000_000 + " ミリ秒、StringBuilder: " + builder / 1_000_000.0 + " ミリ秒");
        }
    }

    StringBuilder sb = new StringBuilder("abc");
    sb.append("def").insert(0, ">>").reverse();
    IO.println("StringBuilder の操作: " + sb);
}
