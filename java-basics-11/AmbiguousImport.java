// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
import java.sql.*;
import java.util.*;

public class AmbiguousImport {
    public static void main(String[] args) {
        Date date = null;
        System.out.println(date);
    }
}
