// 検証しない: 複数ファイルの例。collect.sh で java order/Order.java として実行している
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

// 検証を連ねる順序と、「最初の失敗で止める」「すべて集める」の違い
public class Order {
    record Form(String name, String email) {}

    interface Validator { Optional<String> check(Form form); }

    static final AtomicInteger EXPENSIVE_CALLS = new AtomicInteger();

    // 安い検証：名前が空でないこと
    static final Validator CHEAP = f -> f.name().isBlank() ? Optional.of("名前が空です") : Optional.empty();

    // 高い検証：メールアドレスが登録済みでないこと（データベースへの問い合わせを想定して、1ミリ秒かかる）
    static final Validator EXPENSIVE = f -> {
        EXPENSIVE_CALLS.incrementAndGet();
        try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return f.email().startsWith("dup") ? Optional.of("登録済みのメールアドレスです") : Optional.empty();
    };

    static List<String> stopAtFirst(List<Validator> chain, Form form) {
        for (Validator v : chain) {
            Optional<String> error = v.check(form);
            if (error.isPresent()) return List.of(error.get());
        }
        return List.of();
    }

    static List<String> collectAll(List<Validator> chain, Form form) {
        List<String> errors = new ArrayList<>();
        for (Validator v : chain) v.check(form).ifPresent(errors::add);
        return errors;
    }

    public static void main(String[] args) {
        // 200件の入力：半分は名前が空（安い検証で失敗する）
        List<Form> forms = new ArrayList<>();
        for (int i = 0; i < 200; i++) forms.add(new Form(i % 2 == 0 ? "" : "受験者" + i, "user" + i + "@example.com"));

        record Mode(String label, List<Validator> chain, boolean stop) {}
        List<Mode> modes = List.of(
            new Mode("最初の失敗で止める（安い検証が先）", List.of(CHEAP, EXPENSIVE), true),
            new Mode("最初の失敗で止める（高い検証が先）", List.of(EXPENSIVE, CHEAP), true),
            new Mode("すべて集める（安い検証が先）", List.of(CHEAP, EXPENSIVE), false));
        for (Mode mode : modes) {
            EXPENSIVE_CALLS.set(0);
            long start = System.nanoTime();
            int failed = 0;
            for (Form form : forms) {
                List<String> errors = mode.stop() ? stopAtFirst(mode.chain(), form) : collectAll(mode.chain(), form);
                if (!errors.isEmpty()) failed++;
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("%-30s 高い検証の呼び出し %3d 回、%4d ms、不合格 %d 件%n", mode.label(), EXPENSIVE_CALLS.get(), ms, failed);
        }
    }
}
