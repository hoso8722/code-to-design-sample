// 実行: java OldSwitch.java
void main() {
    int level = 2;

    IO.println("break がない switch 文:");
    switch (level) {
        case 1:
            IO.println("  レベル1");
        case 2:
            IO.println("  レベル2");
        case 3:
            IO.println("  レベル3");
        default:
            IO.println("  default");
    }

    IO.println("break がある switch 文:");
    switch (level) {
        case 1:
            IO.println("  レベル1");
            break;
        case 2:
            IO.println("  レベル2");
            break;
        case 3:
            IO.println("  レベル3");
            break;
        default:
            IO.println("  default");
    }
}
