package example.books;

import java.util.regex.Pattern;

// ログに出す文字列から、機密の値を伏せる
public final class Redactor {
    private static final Pattern SECRET = Pattern.compile("(?i)(password|token|secret)=[^&\\s]+");

    private Redactor() {}

    public static String mask(String text) {
        return text == null ? null : SECRET.matcher(text).replaceAll("$1=****");
    }
}
