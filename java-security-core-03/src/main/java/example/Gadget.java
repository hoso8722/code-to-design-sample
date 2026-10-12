package example;

// 受け取るつもりのないクラス。setterが呼ばれると、表示する(実験用の無害な処理)
public class Gadget {
    public void setCommand(String command) {
        System.out.println("    [Gadget.setCommand が呼ばれた] " + command);
    }
}
