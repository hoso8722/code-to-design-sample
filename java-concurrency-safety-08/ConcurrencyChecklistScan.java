// 実行: java ConcurrencyChecklistScan.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class ConcurrencyChecklistScan {
    // シリーズで再現した問題の「兆候」を、文字列で探すだけの粗い点検。見つかった行は、人が確かめる
    record Rule(String id, Pattern pattern, String advice) {}

    static final List<Rule> RULES = List.of(
            new Rule("#01", Pattern.compile("\\.containsKey\\(|\\.contains\\(.*\\)\\s*\\)"), "確かめてから行う(check-then-act)の疑い。putIfAbsent や compute に置き換えられないか"),
            new Rule("#01", Pattern.compile("Collections\\.synchronized(List|Map|Set)"), "走査するときは、手動でロックしているか。複合操作を1つのロックにまとめているか"),
            new Rule("#02", Pattern.compile("^\\s*(private |public )?(static )?boolean (running|stop\\w*|done|finished|ready|flag\\w*)\\b"), "別のスレッドから読み書きするフラグなら、volatile が必要"),
            new Rule("#03", Pattern.compile("\\.add(Listener|Observer)\\(this\\)|registry\\.add\\(this\\)|new Thread\\(this\\)"), "コンストラクタの中で this を外に出していないか"),
            new Rule("#03", Pattern.compile("if \\(instance == null\\)"), "ダブルチェックドロッキングの疑い。instance は volatile か。ホルダーイディオムにできないか"),
            new Rule("#04", Pattern.compile("synchronized\\s*\\(this\\)\\s*\\{[^}]*\\.(on\\w+|notify\\w+)\\("), "ロックを持ったまま、コールバックを呼んでいないか"),
            new Rule("#05", Pattern.compile("static\\s+(final\\s+)?SimpleDateFormat"), "SimpleDateFormat はスレッドセーフではない。DateTimeFormatter にする"),
            new Rule("#05", Pattern.compile("ThreadLocal<"), "スレッドプールで使うなら、finally で remove() しているか。値を引数で渡せないか"),
            new Rule("#06", Pattern.compile("new LinkedBlockingQueue<>\\(\\)|new LinkedBlockingDeque<>\\(\\)"), "容量が Integer.MAX_VALUE。生産が速いと伸び続ける。容量を決める"),
            new Rule("#06", Pattern.compile("computeIfAbsent\\(.*\\b(map|cache|memo)\\.(put|compute|computeIfAbsent|merge)\\("), "関数の中で同じマップを更新していないか(Recursive update)"));

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("scan-sample-concurrency");
        Files.writeString(dir.resolve("BadConcurrent.java"), """
                class BadConcurrent {
                    static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
                    private boolean running = true;
                    private static Resource instance;
                    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
                    private final Map<String, Integer> cache = new HashMap<>();
                    private final List<Integer> list = Collections.synchronizedList(new ArrayList<>());
                    static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

                    BadConcurrent(Registry registry) {
                        registry.add(this);
                    }

                    void register(String key) {
                        if (!cache.containsKey(key)) {
                            cache.put(key, 1);
                        }
                    }

                    static Resource get() {
                        if (instance == null) {
                            instance = new Resource();
                        }
                        return instance;
                    }

                    synchronized void change(int v) {
                        synchronized (this) { listener.onChange(v); }
                    }

                    long fib(int n) {
                        return cache.computeIfAbsent(n, k -> cache.put(n, 0));
                    }
                }
                """);
        try (var files = Files.list(dir)) {
            for (Path file : files.sorted().toList()) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    for (Rule rule : RULES) {
                        if (rule.pattern().matcher(lines.get(i)).find()) {
                            System.out.printf("%s:%d [%s] %s%n    %s%n", file.getFileName(), i + 1, rule.id(), rule.advice(), lines.get(i).strip());
                        }
                    }
                }
            }
        }
    }
}
