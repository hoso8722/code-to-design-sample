package exam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 動的プロキシ：何も記録せず、呼び出しをそのまま本物へ渡すだけのハンドラ（リフレクションの時間だけを測るため）
public class PassThroughHandler implements InvocationHandler {
    private final Object target;

    private PassThroughHandler(Object target) { this.target = target; }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(Class<T> type, T target) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, new PassThroughHandler(target));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
