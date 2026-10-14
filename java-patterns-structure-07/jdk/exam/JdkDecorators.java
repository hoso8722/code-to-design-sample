// 検証しない: 実行に20秒ほどかかる。collect.sh で java jdk/exam/JdkDecorators.java として実行している
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JdkDecorators {
    public static void main(String[] args) throws Exception {
        // 1. Collections.unmodifiableList：変更を禁止するデコレータ（元のリストの変更は見える）
        List<String> base = new ArrayList<>(List.of("佐藤", "鈴木"));
        List<String> view = Collections.unmodifiableList(base);
        base.add("高橋");
        System.out.println("元のリストに追加したあと、ラップしたリストの大きさ: " + view.size());
        try {
            view.add("伊藤");
        } catch (UnsupportedOperationException e) {
            System.out.println("ラップしたリストへの追加: " + e.getClass().getSimpleName());
        }

        // 2. BufferedInputStream：読み込みにバッファを足すデコレータ
        Path file = Files.createTempFile("decorator", ".bin");
        Files.write(file, new byte[5_000_000]);
        for (int run = 1; run <= 3; run++) {
            long t0 = System.nanoTime();
            long sum = 0;
            try (InputStream in = new FileInputStream(file.toFile())) {
                int b; while ((b = in.read()) != -1) sum += b;
            }
            long t1 = System.nanoTime();
            try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
                int b; while ((b = in.read()) != -1) sum += b;
            }
            long t2 = System.nanoTime();
            System.out.printf("run %d: 5MBを1バイトずつ読む FileInputStream=%.0f ms, BufferedInputStream で包む=%.0f ms (sum=%d)%n", run, (t1 - t0) / 1e6, (t2 - t1) / 1e6, sum);
        }
        Files.delete(file);
    }
}
