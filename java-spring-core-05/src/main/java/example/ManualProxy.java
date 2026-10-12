// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.ManualProxy
package example;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ManualProxy {
    // 呼び出しの前後に、時間の計測をはさむプロキシを、手で作る
    static <T> T withTiming(T target, Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> {
            long start = System.nanoTime();
            try {
                return method.invoke(target, args);
            } finally {
                long ms = (System.nanoTime() - start) / 1_000_000;
                System.out.println("  [計測] " + method.getName() + ": " + ms + " ms");
            }
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    public static void main(String[] args) {
        LoanService real = new LoanServiceImpl();
        LoanService proxy = withTiming(real, LoanService.class);

        System.out.println(proxy.lend("sora", "こころ"));
        System.out.println(proxy.giveBack("sora", "こころ"));
        System.out.println("呼び出し側が持つオブジェクトのクラス: " + proxy.getClass().getName());
        System.out.println("元のオブジェクトのクラス:             " + real.getClass().getName());
    }
}
