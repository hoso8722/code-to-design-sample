// 実行: java PathTraversal.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathTraversal {
    // よくない例:利用者が指定したファイル名を、そのまま連結する
    static String readNaive(Path base, String name) throws IOException {
        return Files.readString(base.resolve(name));
    }

    // 改善した例:正規化してから、基準のフォルダの中かを確かめる
    static String readChecked(Path base, String name) throws IOException {
        Path target = base.resolve(name).normalize();
        if (!target.startsWith(base)) {
            throw new SecurityException("フォルダの外は読めない: " + name);
        }
        return Files.readString(target);
    }

    // さらに改善:シンボリックリンクも解決した、実際のパスで確かめる
    static String readReal(Path base, String name) throws IOException {
        Path target = base.resolve(name).toRealPath();
        if (!target.startsWith(base.toRealPath())) {
            throw new SecurityException("フォルダの外は読めない: " + name);
        }
        return Files.readString(target);
    }

    static void attempt(String label, ThrowingSupplier action) {
        try {
            System.out.println(label + " → " + action.get().strip());
        } catch (Exception e) {
            System.out.println(label + " → 拒否: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    interface ThrowingSupplier {
        String get() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("traversal");
        Path base = root.resolve("uploads");
        Files.createDirectories(base);
        Files.writeString(base.resolve("notice.txt"), "公開のお知らせ");
        Files.writeString(root.resolve("secret.txt"), "★フォルダの外の秘密★");
        // uploads の中に、外のファイルを指すシンボリックリンクを作る
        Files.createSymbolicLink(base.resolve("link.txt"), root.resolve("secret.txt"));

        System.out.println("## 連結しただけのコード");
        attempt("notice.txt        ", () -> readNaive(base, "notice.txt"));
        attempt("../secret.txt     ", () -> readNaive(base, "../secret.txt"));

        System.out.println("## normalize と startsWith で確かめるコード");
        attempt("notice.txt        ", () -> readChecked(base, "notice.txt"));
        attempt("../secret.txt     ", () -> readChecked(base, "../secret.txt"));
        attempt("link.txt (リンク) ", () -> readChecked(base, "link.txt"));

        System.out.println("## toRealPath で確かめるコード");
        attempt("link.txt (リンク) ", () -> readReal(base, "link.txt"));
        attempt("../secret.txt     ", () -> readReal(base, "../secret.txt"));
    }
}
