// 実行: java Unicode.java
public class Unicode {
    public static void main(String[] args) {
        String hiragana = "あいう";
        String emoji = "😀";
        System.out.println("\"あいう\".length() = " + hiragana.length());
        System.out.println("\"😀\".length() = " + emoji.length());
        System.out.println("\"😀\".codePointCount = " + emoji.codePointCount(0, emoji.length()));
        System.out.println("\"a😀b\".length() = " + "a😀b".length());
        System.out.println("\"a😀b\" の文字数(コードポイント) = " + "a😀b".codePointCount(0, "a😀b".length()));
        System.out.println("charAt(1) は半分だけ: " + Character.isSurrogate("a😀b".charAt(1)));
    }
}
