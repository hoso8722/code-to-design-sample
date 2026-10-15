// 検証しない: 複数ファイルの例。collect.sh で java slow/FlowErrors.java として実行している
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

// Flow：受け手の1つが onNext で例外を投げたとき、ほかの受け手に通知は届くか
public class FlowErrors {
    static class Recorder implements Flow.Subscriber<Integer> {
        final String label;
        final boolean failAtThree;
        final List<Integer> received = Collections.synchronizedList(new ArrayList<>());
        final List<String> events = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch finished = new CountDownLatch(1);
        Flow.Subscription subscription;

        Recorder(String label, boolean failAtThree) { this.label = label; this.failAtThree = failAtThree; }

        @Override public void onSubscribe(Flow.Subscription s) { subscription = s; s.request(Long.MAX_VALUE); }
        @Override public void onNext(Integer item) {
            if (failAtThree && item == 3) throw new IllegalStateException("集計で障害");
            received.add(item);
        }
        @Override public void onError(Throwable t) { events.add("onError(" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")"); finished.countDown(); }
        @Override public void onComplete() { events.add("onComplete"); finished.countDown(); }
    }

    public static void main(String[] args) throws Exception {
        Recorder failing = new Recorder("例外を投げる受け手", true);
        Recorder healthy = new Recorder("ふつうの受け手", false);
        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            publisher.subscribe(failing);
            publisher.subscribe(healthy);
            for (int i = 1; i <= 5; i++) publisher.submit(i);
        }
        failing.finished.await(2, TimeUnit.SECONDS);
        healthy.finished.await(2, TimeUnit.SECONDS);
        for (Recorder r : List.of(failing, healthy)) {
            System.out.println(r.label + ": 届いた値 " + r.received + "、通知 " + r.events);
        }
    }
}
