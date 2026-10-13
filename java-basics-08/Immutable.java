// 実行: java Immutable.java
void main() {
    String name = "java";
    String upper = name.toUpperCase();
    IO.println("name = " + name + "、upper = " + upper);   // name は変わらない

    name.concat(" 25");                                     // 戻り値を使わないと、何も起きない
    IO.println("concat の戻り値を捨てた後の name = " + name);

    name = name.concat(" 25");                              // 変数に代入し直すと、新しい文字列を指す
    IO.println("代入し直した後の name = " + name);

    String a = "hello";
    String b = a;
    a = a + " world";
    IO.println("a = " + a + "、b = " + b);                  // b は元の文字列のまま
}
