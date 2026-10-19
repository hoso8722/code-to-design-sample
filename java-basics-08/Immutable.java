// 実行: java Immutable.java
public class Immutable {
    public static void main(String[] args) {
        String name = "java";
        String upper = name.toUpperCase();
        System.out.println("name = " + name + "、upper = " + upper);   // name は変わらない

        name.concat(" 25");                                     // 戻り値を使わないと、何も起きない
        System.out.println("concat の戻り値を捨てた後の name = " + name);

        name = name.concat(" 25");                              // 変数に代入し直すと、新しい文字列を指す
        System.out.println("代入し直した後の name = " + name);

        String a = "hello";
        String b = a;
        a = a + " world";
        System.out.println("a = " + a + "、b = " + b);                  // b は元の文字列のまま
    }
}
