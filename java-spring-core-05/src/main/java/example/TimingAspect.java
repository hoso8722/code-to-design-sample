package example;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// 横断的な処理(計測)を、業務のコードの外に書く
@Aspect
@Component
public class TimingAspect {
    // *Service の、すべての public メソッドを対象にする
    @Around("execution(public * example.*Service*.*(..))")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("  [計測] " + joinPoint.getSignature().getName() + ": " + ms + " ms");
        }
    }
}
