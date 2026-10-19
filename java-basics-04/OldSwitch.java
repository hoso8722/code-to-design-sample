// 実行: java OldSwitch.java
public class OldSwitch {
    public static void main(String[] args) {
        int level = 2;

        System.out.println("break がない switch 文:");
        switch (level) {
            case 1:
                System.out.println("  レベル1");
            case 2:
                System.out.println("  レベル2");
            case 3:
                System.out.println("  レベル3");
            default:
                System.out.println("  default");
        }

        System.out.println("break がある switch 文:");
        switch (level) {
            case 1:
                System.out.println("  レベル1");
                break;
            case 2:
                System.out.println("  レベル2");
                break;
            case 3:
                System.out.println("  レベル3");
                break;
            default:
                System.out.println("  default");
        }
    }
}
