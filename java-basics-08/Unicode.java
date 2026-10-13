// 実行: java Unicode.java
void main() {
    String hiragana = "あいう";
    String emoji = "😀";
    IO.println("\"あいう\".length() = " + hiragana.length());
    IO.println("\"😀\".length() = " + emoji.length());
    IO.println("\"😀\".codePointCount = " + emoji.codePointCount(0, emoji.length()));
    IO.println("\"a😀b\".length() = " + "a😀b".length());
    IO.println("\"a😀b\" の文字数(コードポイント) = " + "a😀b".codePointCount(0, "a😀b".length()));
    IO.println("charAt(1) は半分だけ: " + Character.isSurrogate("a😀b".charAt(1)));
}
