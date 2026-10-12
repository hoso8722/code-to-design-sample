// 実行: java ZipSlip.java
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipSlip {
    // 実験用のZIPを作る:普通のファイルと、名前に ../ を含むファイルが入っている
    static byte[] sampleZip() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (String name : new String[] {"docs/readme.txt", "../evil.txt"}) {
                zip.putNextEntry(new ZipEntry(name));
                zip.write(("中身: " + name).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    // よくない例:エントリ名を、そのまま展開先に連結する
    static void extractNaive(byte[] zipBytes, Path dest) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                Path target = dest.resolve(entry.getName());
                Files.createDirectories(target.getParent());
                Files.write(target, zip.readAllBytes());
            }
        }
    }

    // 改善した例:正規化したパスが、展開先の中にあることを確かめる
    static void extractChecked(byte[] zipBytes, Path dest) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                Path target = dest.resolve(entry.getName()).normalize();
                if (!target.startsWith(dest)) {
                    throw new SecurityException("展開先の外に書こうとした: " + entry.getName());
                }
                Files.createDirectories(target.getParent());
                Files.write(target, zip.readAllBytes());
            }
        }
    }

    static void list(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).sorted()
                    .forEach(p -> System.out.println("  " + root.relativize(p)));
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] zip = sampleZip();

        Path root1 = Files.createTempDirectory("zipslip1");
        Path dest1 = Files.createDirectories(root1.resolve("extract"));
        extractNaive(zip, dest1);
        System.out.println("## エントリ名をそのまま連結して展開した(root 以下のファイル)");
        list(root1);

        Path root2 = Files.createTempDirectory("zipslip2");
        Path dest2 = Files.createDirectories(root2.resolve("extract"));
        System.out.println("## 展開先の中かを確かめて展開した");
        try {
            extractChecked(zip, dest2);
        } catch (SecurityException e) {
            System.out.println("  拒否: " + e.getMessage());
        }
        System.out.println("  書かれたファイル:");
        list(root2);
    }
}
