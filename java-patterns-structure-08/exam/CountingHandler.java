package exam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

// 動的プロキシ：どの interface にも使える、記録用のハンドラ
public class CountingHandler implements InvocationHandler {
    private final Object target;
    public final List<String> calls = new ArrayList<>();

    private CountingHandler(Object target) { this.target = target; }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(Class<T> type, T target, CountingHandler[] holder) {
        CountingHandler handler = new CountingHandler(target);
        holder[0] = handler;
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        calls.add(method.getName());
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
