// 実行: java FixedCompare.java
void main() {
    String input = new String("合格");   // 入力された文字列を想定
    if (input.equals("合格")) {
        IO.println("合格と入力されました");
    } else {
        IO.println("合格と入力されていません");
    }
}
