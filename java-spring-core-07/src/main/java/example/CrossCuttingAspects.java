package example;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

public class CrossCuttingAspects {
    // @Audited の付いたメソッドの、呼び出しの結果を記録する
    @Aspect
    @Order(1)
    public static class AuditAspect {
        @Around("@annotation(audited)")
        public Object audit(ProceedingJoinPoint jp, Audited audited) throws Throwable {
            long start = System.nanoTime();
            String args = Arrays.toString(jp.getArgs());
            try {
                Object result = jp.proceed();
                System.out.println("  [監査] " + audited.action() + " 引数=" + args + " 結果=" + result);
                return result;
            } catch (Throwable e) {
                System.out.println("  [監査] " + audited.action() + " 引数=" + args + " 失敗=" + e.getClass().getSimpleName() + ": " + e.getMessage());
                throw e;
            } finally {
                long ms = (System.nanoTime() - start) / 1_000_000;
                System.out.println("  [監査]   所要時間: " + ms + " ms");
            }
        }
    }

    // @Retry の付いたメソッドを、失敗したら再試行する
    @Aspect
    @Order(2)
    public static class RetryAspect {
        @Around("@annotation(retry)")
        public Object retry(ProceedingJoinPoint jp, Retry retry) throws Throwable {
            Throwable last = null;
            for (int attempt = 1; attempt <= retry.times(); attempt++) {
                try {
                    Object result = jp.proceed();
                    System.out.println("  [再試行] " + attempt + "回目で成功");
                    return result;
                } catch (IllegalStateException e) {
                    last = e;
                    System.out.println("  [再試行] " + attempt + "回目に失敗: " + e.getMessage());
                }
            }
            throw last;
        }
    }

    // 失敗を握りつぶす(よくない例)
    @Aspect
    public static class SwallowAspect {
        @Around("@annotation(example.Swallow)")
        public Object swallow(ProceedingJoinPoint jp) {
            try {
                return jp.proceed();
            } catch (Throwable e) {
                System.out.println("  [握りつぶし] " + e.getMessage());
                return null;
            }
        }
    }
}
