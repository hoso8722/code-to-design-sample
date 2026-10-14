// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

// シリアライズとリフレクションで、Singleton が複数になるかを確かめる
public class Breaking {
    public static void main(String[] args) throws Exception {
        // 1. シリアライズ → デシリアライズ
        ClassicSingleton original = ClassicSingleton.getInstance();
        ClassicSingleton copy = (ClassicSingleton) roundTrip(original);
        System.out.println("古典的な Singleton：デシリアライズ後も同じインスタンスか = " + (original == copy));

        EnumConfig enumOriginal = EnumConfig.getInstance();
        EnumConfig enumCopy = (EnumConfig) roundTrip(enumOriginal);
        System.out.println("enum：デシリアライズ後も同じインスタンスか = " + (enumOriginal == enumCopy));

        // 2. リフレクション
        Constructor<ClassicSingleton> ctor = ClassicSingleton.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassicSingleton another = ctor.newInstance();
        System.out.println("古典的な Singleton：リフレクションで作ったものは同じか = " + (original == another));

        try {
            Constructor<?> enumCtor = EnumConfig.class.getDeclaredConstructors()[0];
            enumCtor.setAccessible(true);
            enumCtor.newInstance("INSTANCE", 0);
            System.out.println("enum：リフレクションで作れた");
        } catch (Exception e) {
            System.out.println("enum：リフレクションで作ろうとすると " + e.getClass().getSimpleName() + "（" + e.getMessage() + "）");
        }
    }

    static Object roundTrip(Object value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(value); }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { return in.readObject(); }
    }
}
