// 実行: java OutOfRange.java
public class OutOfRange {
    public static void main(String[] args) {
        int[] scores = {80, 65, 90};
        try {
            System.out.println(scores[3]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("例外: " + e.getMessage());
        }
        try {
            System.out.println(scores[-1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("例外: " + e.getMessage());
        }
        try {
            int[] negative = new int[-1];
        } catch (NegativeArraySizeException e) {
            System.out.println("例外: " + e.getMessage());
        }
    }
}
