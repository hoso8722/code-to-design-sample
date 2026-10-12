// 実行: java RateLimitDemo.java
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class RateLimitDemo {
    // 時刻を差し替えられる時計。実験では、仮想の時間を進める
    static class Clock implements LongSupplier {
        long millis = 0;
        public long getAsLong() { return millis; }
    }

    // トークンバケット: 容量 capacity、1秒あたり refillPerSecond 個のトークンが補充される
    static class TokenBucket {
        final double capacity, refillPerSecond;
        final LongSupplier clock;
        double tokens;
        long last;

        TokenBucket(double capacity, double refillPerSecond, LongSupplier clock) {
            this.capacity = capacity;
            this.refillPerSecond = refillPerSecond;
            this.clock = clock;
            this.tokens = capacity;
            this.last = clock.getAsLong();
        }

        boolean tryAcquire() {
            long now = clock.getAsLong();
            tokens = Math.min(capacity, tokens + (now - last) / 1000.0 * refillPerSecond);
            last = now;
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }
    }

    static byte[] hash(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100_000, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }

    // 利用者と、パスワードのハッシュを持つ。lockoutKey は、ロックの数え方(利用者名だけ、利用者名+IP)を切り替える
    static class LoginService {
        final Map<String, byte[]> hashes = new HashMap<>();
        final Map<String, byte[]> salts = new HashMap<>();
        final Map<String, Integer> failures = new HashMap<>();
        final Map<String, Long> lockedUntil = new HashMap<>();
        final byte[] dummySalt = new byte[16];
        final byte[] dummyHash;
        final Clock clock;

        LoginService(Clock clock) throws Exception {
            this.clock = clock;
            this.dummyHash = hash("dummy", dummySalt);
        }

        void register(String user, String password) throws Exception {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            salts.put(user, salt);
            hashes.put(user, hash(password, salt));
        }

        // 失敗の理由を、そのまま返す(よくない例)
        String loginVerbose(String user, String password) throws Exception {
            if (!hashes.containsKey(user)) return "そのユーザーは存在しません";
            return MessageDigest.isEqual(hash(password, salts.get(user)), hashes.get(user)) ? "成功" : "パスワードが違います";
        }

        // 失敗の理由を同じにし、存在しない利用者でもハッシュの計算をする
        String loginUniform(String user, String password) throws Exception {
            byte[] salt = salts.getOrDefault(user, dummySalt);
            byte[] expected = hashes.getOrDefault(user, dummyHash);
            boolean ok = MessageDigest.isEqual(hash(password, salt), expected) && hashes.containsKey(user);
            return ok ? "成功" : "ユーザー名かパスワードが違います";
        }

        // ロック方式: key ごとに失敗を数え、limit 回で lockMillis の間ロックする
        String loginWithLockout(String key, String user, String password, int limit, long lockMillis) throws Exception {
            if (lockedUntil.getOrDefault(key, 0L) > clock.getAsLong()) return "ロック中";
            boolean ok = MessageDigest.isEqual(hash(password, salts.get(user)), hashes.get(user));
            if (ok) {
                failures.remove(key);
                return "成功";
            }
            int count = failures.merge(key, 1, Integer::sum);
            if (count >= limit) {
                lockedUntil.put(key, clock.getAsLong() + lockMillis);
                failures.remove(key);
            }
            return "失敗";
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. 制限なし: 4桁のPIN(10,000通り)を、すべて試す");
        byte[] secret = MessageDigest.getInstance("SHA-256").digest("7391".getBytes());
        long start = System.nanoTime();
        int attempts = 0;
        String found = null;
        for (int pin = 0; pin < 10_000; pin++) {
            attempts++;
            String guess = String.format("%04d", pin);
            if (MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(guess.getBytes()), secret)) {
                found = guess;
                break;
            }
        }
        System.out.printf("  %d 回の試行で %s を発見(%.1f ms)%n", attempts, found, (System.nanoTime() - start) / 1e6);

        System.out.println("## 2. トークンバケット(容量5、1秒に1個補充)で、10ミリ秒ごとに試行する");
        Clock clock = new Clock();
        TokenBucket bucket = new TokenBucket(5, 1, clock);
        int allowed = 0;
        for (int i = 0; i < 10_000; i++) {
            if (bucket.tryAcquire()) allowed++;
            clock.millis += 10;
        }
        System.out.printf("  仮想の100秒間に、許された試行: %d 回(10,000通り全部を試すには、約 %.1f 時間)%n", allowed, 10_000 / 3600.0);

        LoginService service = new LoginService(new Clock());
        service.register("alice", "alice-correct-password");

        System.out.println("## 3. ロック方式: 利用者名だけで数える(5回失敗で15分ロック)");
        Clock c3 = new Clock();
        LoginService s3 = new LoginService(c3);
        s3.register("alice", "alice-correct-password");
        for (int i = 0; i < 5; i++) s3.loginWithLockout("alice", "alice", "guess" + i, 5, 15 * 60_000);
        System.out.println("  攻撃者が5回失敗させた後、本人のログイン: " + s3.loginWithLockout("alice", "alice", "alice-correct-password", 5, 15 * 60_000));

        System.out.println("## 4. ロック方式: 利用者名とIPで数える");
        LoginService s4 = new LoginService(new Clock());
        s4.register("alice", "alice-correct-password");
        for (int i = 0; i < 5; i++) s4.loginWithLockout("alice@203.0.113.9", "alice", "guess" + i, 5, 15 * 60_000);
        System.out.println("  攻撃者(203.0.113.9)の次の試行: " + s4.loginWithLockout("alice@203.0.113.9", "alice", "guess", 5, 15 * 60_000));
        System.out.println("  本人(198.51.100.7)のログイン: " + s4.loginWithLockout("alice@198.51.100.7", "alice", "alice-correct-password", 5, 15 * 60_000));

        System.out.println("## 5. 応答の違い(存在する利用者 alice、存在しない利用者 nobody)");
        System.out.println("  verbose  alice  : " + service.loginVerbose("alice", "wrong"));
        System.out.println("  verbose  nobody : " + service.loginVerbose("nobody", "wrong"));
        System.out.println("  uniform  alice  : " + service.loginUniform("alice", "wrong"));
        System.out.println("  uniform  nobody : " + service.loginUniform("nobody", "wrong"));

        System.out.println("## 6. 応答時間の違い(各20回の平均)");
        for (int i = 0; i < 5; i++) { service.loginVerbose("alice", "x"); service.loginUniform("alice", "x"); } // ウォームアップ
        System.out.printf("  verbose  alice  : %.1f ms%n", average(() -> service.loginVerbose("alice", "wrong")));
        System.out.printf("  verbose  nobody : %.1f ms%n", average(() -> service.loginVerbose("nobody", "wrong")));
        System.out.printf("  uniform  alice  : %.1f ms%n", average(() -> service.loginUniform("alice", "wrong")));
        System.out.printf("  uniform  nobody : %.1f ms%n", average(() -> service.loginUniform("nobody", "wrong")));
    }

    interface Call { Object run() throws Exception; }

    static double average(Call call) throws Exception {
        long total = 0;
        for (int i = 0; i < 20; i++) {
            long start = System.nanoTime();
            call.run();
            total += System.nanoTime() - start;
        }
        return total / 20 / 1e6;
    }
}
