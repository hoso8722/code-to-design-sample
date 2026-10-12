package example;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

// 名前が audit で始まるメソッドの呼び出しを、記録する
@Aspect
public class AuditAspect {
    @Around("execution(* example..*.audit*(..))")
    public Object record(ProceedingJoinPoint jp) throws Throwable {
        System.out.println("    [アスペクト] " + jp.getSignature().getDeclaringType().getSimpleName() + "." + jp.getSignature().getName() + " を記録");
        return jp.proceed();
    }
}
