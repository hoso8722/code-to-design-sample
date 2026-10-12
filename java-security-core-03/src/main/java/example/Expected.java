package example;

import java.io.Serializable;

// 受け取るつもりの、安全なクラス
public class Expected implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name = "expected";
}
