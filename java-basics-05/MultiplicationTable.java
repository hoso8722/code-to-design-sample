// 実行: java MultiplicationTable.java
void main() {
    for (int row = 1; row <= 9; row++) {
        for (int col = 1; col <= 9; col++) {
            IO.print(String.format("%3d", row * col));
        }
        IO.println();
    }
}
