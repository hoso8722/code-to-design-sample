// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

// depth 回だけ関数を呼び出した先で失敗し、最初の呼び出し元で失敗を受け取るまでの時間
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class ThrowCost {
    @Param({"1", "10", "100"})
    int depth;

    // スタックトレースを作らない例外（4つ目の引数 writableStackTrace を false にする）
    static class NoTraceException extends RuntimeException {
        NoTraceException(String message) {
            super(message, null, false, false);
        }
    }

    // 戻り値 -1 で失敗を伝える
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    int failWithCode(int d) {
        if (d <= 1) {
            return -1;
        }
        return failWithCode(d - 1);
    }

    // 例外で失敗を伝える
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    int failWithException(int d) {
        if (d <= 1) {
            throw new IllegalArgumentException("失敗");
        }
        return failWithException(d - 1);
    }

    // スタックトレースを作らない例外で失敗を伝える
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    int failWithNoTrace(int d) {
        if (d <= 1) {
            throw new NoTraceException("失敗");
        }
        return failWithNoTrace(d - 1);
    }

    // 例外を作るだけで、投げない（戻り値として返す）
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    RuntimeException createException(int d) {
        if (d <= 1) {
            return new IllegalArgumentException("失敗");
        }
        return createException(d - 1);
    }

    @Benchmark
    public int returnCode() {
        return failWithCode(depth);
    }

    @Benchmark
    public int exception() {
        try {
            return failWithException(depth);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    @Benchmark
    public RuntimeException createOnly() {
        return createException(depth);
    }

    @Benchmark
    public int noTrace() {
        try {
            return failWithNoTrace(depth);
        } catch (NoTraceException e) {
            return -1;
        }
    }
}
