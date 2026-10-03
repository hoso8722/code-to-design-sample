// 実行: java Frames.java
public class Frames {
    // depth 回だけ自分を呼び出した先で、例外を作って返す（投げない）
    static Exception create(int depth) {
        if (depth <= 1) {
            return new IllegalArgumentException("失敗");
        }
        return create(depth - 1);
    }

    public static void main(String[] args) {
        int base = new Exception().getStackTrace().length;  // main までの行数
        for (int depth : new int[] {1, 10, 100}) {
            Exception e = create(depth);
            int frames = e.getStackTrace().length - base;
            System.out.println("深さ " + depth + ": スタックトレースは main より先に " + frames + " 行");
        }
    }
}
