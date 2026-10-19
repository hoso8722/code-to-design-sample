// 実行: java BasicTypes.java
public class BasicTypes {
    public static void main(String[] args) {
        int age = 20;                 // 整数(32ビット)
        long population = 8_000_000_000L; // 大きな整数(64ビット)。数字の区切りに _ が使える
        double height = 172.5;        // 小数(64ビット)
        boolean passed = true;        // true か false
        char grade = 'A';             // 1文字(シングルクォート)
        String name = "田中";          // 文字列(ダブルクォート)

        System.out.println(age);
        System.out.println(population);
        System.out.println(height);
        System.out.println(passed);
        System.out.println(grade);
        System.out.println(name);

        System.out.println("int の最小値と最大値: " + Integer.MIN_VALUE + " 〜 " + Integer.MAX_VALUE);
        System.out.println("long の最大値: " + Long.MAX_VALUE);
        System.out.println("byte: " + Byte.MIN_VALUE + " 〜 " + Byte.MAX_VALUE);
        System.out.println("double の最大値: " + Double.MAX_VALUE);
    }
}
