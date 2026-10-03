// 実行: java ReadText.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReadText {
    // "受験者0000001,70,65,80" の形式の行から、合計点を求める
    static int totalOf(String line) {
        String[] parts = line.split(",");
        return Integer.parseInt(parts[1]) + Integer.parseInt(parts[2]) + Integer.parseInt(parts[3]);
    }

    // 1. Files.readAllLines：すべての行を、リストとして読む
    static long readAllLines(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        report("readAllLines");
        long sum = 0;
        for (String line : lines) {
            sum += totalOf(line);
        }
        return sum;
    }

    // 2. Files.readString：全体を、1つの文字列として読む
    static long readString(Path file) throws IOException {
        String text = Files.readString(file);
        report("readString");
        long sum = 0;
        for (String line : text.split("\n")) {
            sum += totalOf(line);
        }
        return sum;
    }

    // 3. Files.lines：行のストリームとして、1行ずつ読む
    static long lines(Path file) throws IOException {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.mapToLong(ReadText::totalOf).sum();
        }
    }

    // 4. BufferedReader：readLine で、1行ずつ読む
    static long bufferedReader(Path file) throws IOException {
        long sum = 0;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                sum += totalOf(line);
            }
        }
        return sum;
    }

    static long baseline;  // ファイルを読む前に使っていたヒープ

    static long usedHeap() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    static boolean measuringMemory = false;

    // 読み込んだデータを保持している時点で、GCのあとに残っているメモリを表示する（メモリを調べる回だけ）
    static void report(String name) {
        if (measuringMemory) {
            System.out.printf("%s が読み込んだデータ: %.0f MB%n", name, (usedHeap() - baseline) / 1e6);
        }
    }

    interface Reader {
        long read(Path file) throws IOException;
    }

    // 5回計って、中央値（ミリ秒）を表示する。計る前に、毎回GCを実行する
    static void measureTime(String name, Reader reader, Path file) throws IOException {
        double[] times = new double[5];
        long sum = 0;
        for (int i = 0; i < times.length; i++) {
            System.gc();
            long start = System.nanoTime();
            sum = reader.read(file);
            times[i] = (System.nanoTime() - start) / 1e6;
        }
        Arrays.sort(times);
        System.out.printf("%s: %.0f ms（合計 %,d）%n", name, times[times.length / 2], sum);
    }

    public static void main(String[] args) throws IOException {
        int lineCount = args.length > 0 ? Integer.parseInt(args[0]) : 2_000_000;
        Path file = Files.createTempFile("scores", ".csv");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                for (int i = 1; i <= lineCount; i++) {
                    writer.write(String.format("受験者%07d,%d,%d,%d%n", i, i % 101, (i * 7) % 101, (i * 13) % 101));
                }
            }
            System.out.printf("ファイル: %,d行、%.1f MB%n", lineCount, Files.size(file) / 1e6);

            measureTime("Files.readAllLines", ReadText::readAllLines, file);
            measureTime("Files.readString", ReadText::readString, file);
            measureTime("Files.lines", ReadText::lines, file);
            measureTime("BufferedReader", ReadText::bufferedReader, file);

            measuringMemory = true;
            baseline = usedHeap();
            readAllLines(file);
            baseline = usedHeap();
            readString(file);
        } finally {
            Files.delete(file);
        }
    }
}
