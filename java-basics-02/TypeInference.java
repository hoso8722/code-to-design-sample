// 実行: java TypeInference.java
void main() {
    var count = 10;           // int と決まる
    var rate = 0.1;           // double と決まる
    var name = "田中";         // String と決まる
    var scores = new int[] {80, 65, 90}; // int[] と決まる

    IO.println(((Object) count).getClass().getName());
    IO.println(((Object) rate).getClass().getName());
    IO.println(name.getClass().getName());
    IO.println(scores.getClass().getSimpleName());

    // 暗黙の型の拡大(小さい型から大きい型へは、そのまま代入できる)
    long wide = count;
    double decimal = count;
    IO.println(wide + " " + decimal);

    // 明示的なキャスト(大きい型から小さい型へは、書いて指示する。小数点以下は切り捨て)
    int narrow = (int) 3.99;
    IO.println(narrow);
}
