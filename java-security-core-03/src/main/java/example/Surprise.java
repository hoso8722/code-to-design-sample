package example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

// 受け取るつもりのないクラス。復元されるときに、readObject が実行される(実験では、表示するだけの無害な処理)
public class Surprise implements Serializable {
    private static final long serialVersionUID = 1L;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        System.out.println("    [Surprise.readObject が実行された]");
    }
}
