// 実行: java StringMethods.java
public class StringMethods {
    public static void main(String[] args) {
        String text = "  Java Programming  ";
        System.out.println("length: " + text.length());
        System.out.println("trim: [" + text.trim() + "]");
        System.out.println("strip: [" + text.strip() + "]");

        String s = "Hello, World";
        System.out.println("charAt(0): " + s.charAt(0));
        System.out.println("substring(7): " + s.substring(7));
        System.out.println("substring(0, 5): " + s.substring(0, 5));
        System.out.println("indexOf(\"o\"): " + s.indexOf("o"));
        System.out.println("lastIndexOf(\"o\"): " + s.lastIndexOf("o"));
        System.out.println("indexOf(\"x\"): " + s.indexOf("x"));
        System.out.println("contains(\"World\"): " + s.contains("World"));
        System.out.println("startsWith(\"Hello\"): " + s.startsWith("Hello"));
        System.out.println("replace(\"o\", \"0\"): " + s.replace("o", "0"));
        System.out.println("toUpperCase(): " + s.toUpperCase());
        System.out.println("equalsIgnoreCase: " + s.equalsIgnoreCase("hello, world"));

        String csv = "田中,75,A";
        String[] parts = csv.split(",");
        System.out.println("split の要素数: " + parts.length + "、" + java.util.Arrays.toString(parts));
        System.out.println("join: " + String.join("/", parts));
        System.out.println("repeat: " + "=".repeat(10));
        System.out.println("isEmpty: " + "".isEmpty() + "、isBlank: " + "   ".isBlank());
    }
}
