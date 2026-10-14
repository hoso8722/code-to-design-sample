// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.lang.reflect.Proxy;
import java.util.List;

// JDK の動的プロキシ：interface ごとにクラスを書かずに、記録用のプロキシを作る
public class DynamicDemo {
    public static void main(String[] args) {
        CountingHandler[] holder = new CountingHandler[1];
        StudentRepository repository = CountingHandler.wrap(StudentRepository.class, new DatabaseRepository(), holder);

        repository.find("s1");
        repository.ids();
        repository.toString();   // Object のメソッドのうち、toString・hashCode・equals は、ハンドラを通る
        repository.hashCode();
        repository.equals(repository);
        repository.getClass();   // getClass は final なので、ハンドラを通らない
        System.out.println("記録された呼び出し: " + holder[0].calls);
        System.out.println("プロキシのクラスは Proxy か: " + Proxy.isProxyClass(repository.getClass()));
        System.out.println("プロキシのクラスは StudentRepository を実装しているか: " + (repository instanceof StudentRepository));
        System.out.println("プロキシのクラスは DatabaseRepository か: " + (repository instanceof DatabaseRepository));

        // 同じハンドラが、別の interface にも使える
        CountingHandler[] holder2 = new CountingHandler[1];
        Runnable runnable = CountingHandler.wrap(Runnable.class, () -> System.out.println("Runnable を実行した"), holder2);
        runnable.run();
        System.out.println("Runnable の記録: " + holder2[0].calls);

        // クラス（interface ではないもの）には使えない
        try {
            Proxy.newProxyInstance(DatabaseRepository.class.getClassLoader(), new Class<?>[] {DatabaseRepository.class}, (p, m, a) -> null);
        } catch (IllegalArgumentException e) {
            System.out.println("クラスを渡したとき: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
