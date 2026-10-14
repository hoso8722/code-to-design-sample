package exam;

import java.io.Serializable;

// 古典的な Singleton（Serializable を実装している）
public class ClassicSingleton implements Serializable {
    private static final ClassicSingleton INSTANCE = new ClassicSingleton();

    private ClassicSingleton() {}

    public static ClassicSingleton getInstance() { return INSTANCE; }
}
