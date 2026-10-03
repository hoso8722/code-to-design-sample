// 実行: java Bom.java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Bom {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("bom", ".csv");
        try {
            // 先頭に BOM（EF BB BF）が付いた UTF-8 のファイル
            byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] body = "名前,点数\n".getBytes(StandardCharsets.UTF_8);
            byte[] all = new byte[bom.length + body.length];
            System.arraycopy(bom, 0, all, 0, bom.length);
            System.arraycopy(body, 0, all, bom.length, body.length);
            Files.write(file, all);

            String first = Files.readAllLines(file).get(0);
            System.out.println("1行目の文字数: " + first.length());
            System.out.println("\"名前,点数\" と等しいか: " + first.equals("名前,点数"));
            System.out.printf("先頭の文字: U+%04X%n", (int) first.charAt(0));

            // 先頭が U+FEFF なら取り除く
            String cleaned = first.startsWith("﻿") ? first.substring(1) : first;
            System.out.println("取り除いたあと: " + cleaned.equals("名前,点数"));
        } finally {
            Files.delete(file);
        }
    }
}
