// 検証しない: 複数ファイルの例。collect.sh で java slow/Slow.java <モード> として実行している
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicInteger;

// 受け手が遅い（1件に2ミリ秒かかる）とき、200件を通知するのにかかる時間と、届いた件数
public class Slow {
    static final int N = 200;
    static final int WORK_MS = 2;

    static void work() {
        try {
            Thread.sleep(WORK_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 受け手：1件ずつ要求し、1件に2ミリ秒かかる
    static class SlowSubscriber implements Flow.Subscriber<Integer> {
        final AtomicInteger received = new AtomicInteger();
        final CountDownLatch done = new CountDownLatch(1);
        Flow.Subscription subscription;

        @Override public void onSubscribe(Flow.Subscription s) { subscription = s; s.request(1); }
        @Override public void onNext(Integer item) { work(); received.incrementAndGet(); subscription.request(1); }
        @Override public void onError(Throwable t) { done.countDown(); }
        @Override public void onComplete() { done.countDown(); }
    }

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        long start = System.nanoTime();
        switch (mode) {
            case "sync" -> {
                AtomicInteger received = new AtomicInteger();
                for (int i = 0; i < N; i++) { work(); received.incrementAndGet(); }   // 送り手が、受け手を直接呼ぶ
                long ms = (System.nanoTime() - start) / 1_000_000;
                System.out.printf("%-14s 送り手が終わるまで %4d ms、届いた件数 %d、捨てた件数 0%n", mode, ms, received.get());
            }
            case "submit16", "submit256", "offer16" -> {
                int buffer = mode.endsWith("256") ? 256 : 16;
                AtomicInteger dropped = new AtomicInteger();
                SlowSubscriber subscriber = new SlowSubscriber();
                long producerMs;
                try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>(java.util.concurrent.ForkJoinPool.commonPool(), buffer)) {
                    publisher.subscribe(subscriber);
                    for (int i = 0; i < N; i++) {
                        if (mode.startsWith("offer")) {
                            publisher.offer(i, (s, item) -> { dropped.incrementAndGet(); return false; });   // 受け手が追いつかなければ、捨てる
                        } else {
                            publisher.submit(i);   // バッファが満杯なら、空くまで待つ
                        }
                    }
                    producerMs = (System.nanoTime() - start) / 1_000_000;
                }
                subscriber.done.await();
                long totalMs = (System.nanoTime() - start) / 1_000_000;
                System.out.printf("%-14s 送り手が終わるまで %4d ms、全件の処理が終わるまで %4d ms、届いた件数 %d、捨てた件数 %d%n",
                    mode, producerMs, totalMs, subscriber.received.get(), dropped.get());
            }
        }
    }
}
