// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.ArrayList;
import java.util.List;

// キャッシュあり・なしで、繰り返しの判定にかかる時間を比べる
public class Speed {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 1000; i++) students.add(new Student("受験者" + i, i % 101, (i * 7) % 101, (i * 13) % 101));
        int repeat = 100;   // 同じ1000人を100回判定する（合計10万回）

        for (int run = 1; run <= 5; run++) {
            Judge plain = new ScoreJudge();
            TimingJudge timedPlain = new TimingJudge(plain);
            long t0 = System.nanoTime();
            int passed = 0;
            for (int r = 0; r < repeat; r++) for (Student s : students) if (timedPlain.passes(s)) passed++;
            long t1 = System.nanoTime();

            TimingJudge timedCached = new TimingJudge(new CachingJudge(new ScoreJudge()));
            int passed2 = 0;
            for (int r = 0; r < repeat; r++) for (Student s : students) if (timedCached.passes(s)) passed2++;
            long t2 = System.nanoTime();
            System.out.printf("run %d: キャッシュなし=%.0f ms, あり=%.0f ms (合格 %d / %d)%n", run, (t1 - t0) / 1e6, (t2 - t1) / 1e6, passed, passed2);
        }
    }
}
