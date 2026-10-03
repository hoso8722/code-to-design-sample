// 実行: java Visible.java
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Visible {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("result", ".txt");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write("Aさん,合格\n");
                System.out.println("write のあと: " + Files.size(file) + "バイト");

                writer.flush();
                System.out.println("flush のあと: " + Files.size(file) + "バイト");

                writer.write("Dさん,不合格\n");
                System.out.println("2回目の write のあと: " + Files.size(file) + "バイト");
            }
            System.out.println("close のあと: " + Files.size(file) + "バイト");
        } finally {
            Files.delete(file);
        }
    }
}
