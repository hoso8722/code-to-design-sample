// 実行: java StringMethods.java
void main() {
    String text = "  Java Programming  ";
    IO.println("length: " + text.length());
    IO.println("trim: [" + text.trim() + "]");
    IO.println("strip: [" + text.strip() + "]");

    String s = "Hello, World";
    IO.println("charAt(0): " + s.charAt(0));
    IO.println("substring(7): " + s.substring(7));
    IO.println("substring(0, 5): " + s.substring(0, 5));
    IO.println("indexOf(\"o\"): " + s.indexOf("o"));
    IO.println("lastIndexOf(\"o\"): " + s.lastIndexOf("o"));
    IO.println("indexOf(\"x\"): " + s.indexOf("x"));
    IO.println("contains(\"World\"): " + s.contains("World"));
    IO.println("startsWith(\"Hello\"): " + s.startsWith("Hello"));
    IO.println("replace(\"o\", \"0\"): " + s.replace("o", "0"));
    IO.println("toUpperCase(): " + s.toUpperCase());
    IO.println("equalsIgnoreCase: " + s.equalsIgnoreCase("hello, world"));

    String csv = "田中,75,A";
    String[] parts = csv.split(",");
    IO.println("split の要素数: " + parts.length + "、" + java.util.Arrays.toString(parts));
    IO.println("join: " + String.join("/", parts));
    IO.println("repeat: " + "=".repeat(10));
    IO.println("isEmpty: " + "".isEmpty() + "、isBlank: " + "   ".isBlank());
}
