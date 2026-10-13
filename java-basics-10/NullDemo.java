// 実行: java NullDemo.java
public class NullDemo {
    public static void main(String[] args) {
        String name = null;
        System.out.println("null を表示: " + name);
        System.out.println("null と比べる: " + (name == null));

        String[] names = new String[2];
        System.out.println("配列の要素の初期値: " + names[0]);

        try {
            System.out.println(name.length());
        } catch (NullPointerException e) {
            System.out.println("NullPointerException: " + e.getMessage());
        }

        // null かもしれない値を、安全に扱う
        if (name != null && name.length() > 0) {
            System.out.println("名前あり");
        } else {
            System.out.println("名前なし");
        }
        System.out.println("定数側から equals: " + "田中".equals(name));
        System.out.println("Objects.equals: " + java.util.Objects.equals(name, null));
    }
}
