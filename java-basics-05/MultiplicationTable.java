// 実行: java MultiplicationTable.java
public class MultiplicationTable {
    public static void main(String[] args) {
        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                System.out.print(String.format("%3d", row * col));
            }
            System.out.println();
        }
    }
}
