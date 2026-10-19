// 実行: java BrokenCompare.java
public class BrokenCompare {
    public static void main(String[] args) {
        String input = new String("合格");   // 入力された文字列を想定
        if (input == "合格") {
            System.out.println("合格と入力されました");
        } else {
            System.out.println("合格と入力されていません");
        }
    }
}
