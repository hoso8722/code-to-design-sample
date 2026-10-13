// 実行: java OutOfRange.java
void main() {
    int[] scores = {80, 65, 90};
    try {
        IO.println(scores[3]);
    } catch (ArrayIndexOutOfBoundsException e) {
        IO.println("例外: " + e.getMessage());
    }
    try {
        IO.println(scores[-1]);
    } catch (ArrayIndexOutOfBoundsException e) {
        IO.println("例外: " + e.getMessage());
    }
    try {
        int[] negative = new int[-1];
    } catch (NegativeArraySizeException e) {
        IO.println("例外: " + e.getMessage());
    }
}
