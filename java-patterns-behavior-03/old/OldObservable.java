// 検証しない: 非推奨の警告を確かめるための例。collect.sh で javac -Xlint:deprecation としてコンパイルしている
import java.util.Observable;
import java.util.Observer;

public class OldObservable {
    static class Source extends Observable {
        void change() { setChanged(); notifyObservers("変わった"); }
    }

    public static void main(String[] args) {
        Source source = new Source();
        Observer observer = (o, arg) -> System.out.println("通知: " + arg);
        source.addObserver(observer);
        source.change();
    }
}
