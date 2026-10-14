// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class Bench {
    // 接続に時間がかからない、軽い本物
    static class Light implements StudentRepository {
        public Student find(String id) { return new Student(id, "x", id.length()); }
        public List<String> ids() { return List.of(); }
    }

    static final int N = 5_000_000;

    public static void main(String[] args) {
        StudentRepository direct = new Light();
        LoggingRepository handwritten = new LoggingRepository(new Light());
        CountingHandler[] holder = new CountingHandler[1];
        StudentRepository dynamic = CountingHandler.wrap(StudentRepository.class, new Light(), holder);
        for (int run = 1; run <= 5; run++) {
            long sum = 0;
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) sum += direct.find("s1").total();
            long t1 = System.nanoTime();
            for (int i = 0; i < N; i++) sum += handwritten.find("s1").total();
            long t2 = System.nanoTime();
            holder[0].calls.clear();
            for (int i = 0; i < N; i++) {
                sum += dynamic.find("s1").total();
                if (holder[0].calls.size() > 1000) holder[0].calls.clear();
            }
            long t3 = System.nanoTime();
            System.out.printf("run %d: 直接=%.1f ns, 手で書いたプロキシ=%.1f ns, 動的プロキシ=%.1f ns (sum=%d)%n",
                run, (t1 - t0) / (double) N, (t2 - t1) / (double) N, (t3 - t2) / (double) N, sum);
        }
    }
}
