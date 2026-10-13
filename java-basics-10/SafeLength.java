// 実行: java SafeLength.java
public class SafeLength {
    static int lengthOrZero(String text) {
        return text == null ? 0 : text.length();
    }

    public static void main(String[] args) {
        System.out.println(lengthOrZero("田中"));
        System.out.println(lengthOrZero(""));
        System.out.println(lengthOrZero(null));
    }
}
