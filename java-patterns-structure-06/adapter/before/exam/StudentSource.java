package exam;

// 内側のコードが期待する形：1行を Student にして返す
public interface StudentSource {
    Student parse(String line);
}
