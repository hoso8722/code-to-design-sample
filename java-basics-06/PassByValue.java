// 実行: java PassByValue.java
void increment(int number) {
    number = number + 1;                 // 引数のコピーを変更しているだけ
    IO.println("  メソッドの中: " + number);
}

void replaceFirst(int[] array) {
    array[0] = 999;                      // 参照先の配列の中身を変更する
}

void replaceArray(int[] array) {
    array = new int[] {7, 7, 7};         // 参照のコピーを、別の配列に向け直すだけ
    IO.println("  メソッドの中: " + java.util.Arrays.toString(array));
}

void main() {
    int value = 10;
    increment(value);
    IO.println("基本型: 呼び出したあとの value = " + value);

    int[] scores = {80, 65, 90};
    replaceFirst(scores);
    IO.println("配列の中身を変更: " + java.util.Arrays.toString(scores));

    int[] other = {1, 2, 3};
    replaceArray(other);
    IO.println("配列を差し替え: 呼び出したあとの other = " + java.util.Arrays.toString(other));
}
