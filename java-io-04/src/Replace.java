// 実行: java Replace.java
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Replace {
    // 3行を書く途中、2行目のあとで失敗する（書き込みの途中で止まる状況を、例外で模擬する）
    static void writeAndFail(BufferedWriter writer) throws IOException {
        writer.write("Aさん,合格\n");
        writer.write("Dさん,不合格\n");
        writer.flush();
        throw new IOException("書き込みの途中で失敗しました");
    }

    // 誤り：元のファイルを、直接書き換える
    static void overwrite(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writeAndFail(writer);
        }
    }

    // 正しい：一時ファイルに書いてから、名前を変えて置き換える
    static void replaceSafely(Path file) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp)) {
                writeAndFail(writer);
            }
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("replace");
        Path file = dir.resolve("result.csv");
        List<String> original = List.of("Aさん,合格", "Cさん,合格", "Dさん,不合格");
        try {
            Files.write(file, original);
            try {
                overwrite(file);
            } catch (IOException e) {
                System.out.println("直接書き換えて失敗したあと: " + Files.readAllLines(file));
            }

            Files.write(file, original);
            try {
                replaceSafely(file);
            } catch (IOException e) {
                System.out.println("一時ファイルに書いて失敗したあと: " + Files.readAllLines(file));
            }
        } finally {
            Files.deleteIfExists(file);
            Files.delete(dir);
        }
    }
}
