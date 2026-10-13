// 実行: mvn -q package && java -jar target/jcstress.jar -m quick -t MemoryModelTests
package example;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

public class MemoryModelTests {

    // 書いてから読む(Dekker型)。通常の変数
    @JCStressTest
    @Outcome(id = {"0, 1", "1, 0", "1, 1"}, expect = Expect.ACCEPTABLE, desc = "逐次一貫で説明できる結果")
    @Outcome(id = "0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "両方とも0。逐次一貫では説明できない")
    @State
    public static class StoreLoadPlain {
        int x, y;

        @Actor
        public void actor1(II_Result r) {
            x = 1;
            r.r1 = y;
        }

        @Actor
        public void actor2(II_Result r) {
            y = 1;
            r.r2 = x;
        }
    }

    // 同じ形で、volatile にする
    @JCStressTest
    @Outcome(id = {"0, 1", "1, 0", "1, 1"}, expect = Expect.ACCEPTABLE, desc = "逐次一貫で説明できる結果")
    @Outcome(id = "0, 0", expect = Expect.FORBIDDEN, desc = "volatile では起きてはならない")
    @State
    public static class StoreLoadVolatile {
        volatile int x, y;

        @Actor
        public void actor1(II_Result r) {
            x = 1;
            r.r1 = y;
        }

        @Actor
        public void actor2(II_Result r) {
            y = 1;
            r.r2 = x;
        }
    }

    // メッセージ渡し。data を書いてから ready を書く。読む側は ready を読んでから data を読む。通常の変数
    @JCStressTest
    @Outcome(id = {"0, 0", "0, 42", "1, 42"}, expect = Expect.ACCEPTABLE, desc = "ready を見たなら data も見える、または ready をまだ見ていない")
    @Outcome(id = "1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "ready を見たのに data が古い")
    @State
    public static class MessagePassingPlain {
        int data;
        int ready;

        @Actor
        public void writer() {
            data = 42;
            ready = 1;
        }

        @Actor
        public void reader(II_Result r) {
            r.r1 = ready;
            r.r2 = data;
        }
    }

    // ready を volatile にする
    @JCStressTest
    @Outcome(id = {"0, 0", "0, 42", "1, 42"}, expect = Expect.ACCEPTABLE, desc = "ready を見たなら data も見える、または ready をまだ見ていない")
    @Outcome(id = "1, 0", expect = Expect.FORBIDDEN, desc = "volatile の ready を見たなら、data は必ず見える")
    @State
    public static class MessagePassingVolatile {
        int data;
        volatile int ready;

        @Actor
        public void writer() {
            data = 42;
            ready = 1;
        }

        @Actor
        public void reader(II_Result r) {
            r.r1 = ready;
            r.r2 = data;
        }
    }
}
