// 実行: java ParseOrDefault.java
public class ParseOrDefault {
    static int parseOrDefault(String text, int defaultValue) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        System.out.println(parseOrDefault("75", 0));
        System.out.println(parseOrDefault(" 80 ", 0));
        System.out.println(parseOrDefault("abc", -1));
        System.out.println(parseOrDefault("", -1));
    }
}
