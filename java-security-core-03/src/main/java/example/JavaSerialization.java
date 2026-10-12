// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.JavaSerialization
package example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerialization {
    static byte[] serialize(Object o) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(o);
        }
        return bytes.toByteArray();
    }

    // よくない例:外から来たバイト列を、そのまま復元してから、型を確かめる
    static Expected readNaive(byte[] data) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Expected) in.readObject();
        }
    }

    // 改善した例:復元の前に、許可するクラスを限るフィルターを設定する
    static Expected readFiltered(byte[] data) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("example.Expected;!*"));
            return (Expected) in.readObject();
        }
    }

    interface Reader {
        Expected read(byte[] data) throws Exception;
    }

    static void attempt(String label, Reader reader, byte[] data) {
        System.out.println(label);
        try {
            System.out.println("    成功: " + reader.read(data).name);
        } catch (Exception e) {
            System.out.println("    失敗: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] good = serialize(new Expected());
        byte[] bad = serialize(new Surprise());

        System.out.println("## フィルターなし");
        attempt("  Expected のバイト列", JavaSerialization::readNaive, good);
        attempt("  Surprise のバイト列(想定外のクラス)", JavaSerialization::readNaive, bad);

        System.out.println("## フィルターあり(example.Expected だけを許可)");
        attempt("  Expected のバイト列", JavaSerialization::readFiltered, good);
        attempt("  Surprise のバイト列(想定外のクラス)", JavaSerialization::readFiltered, bad);
    }
}
