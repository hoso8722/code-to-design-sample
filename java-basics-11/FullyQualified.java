// 実行: java FullyQualified.java
import java.util.*;

public class FullyQualified {
    public static void main(String[] args) {
        Date utilDate = new Date(0L);                // java.util.Date
        java.sql.Date sqlDate = new java.sql.Date(0L); // 同じ名前は完全修飾名で区別する
        System.out.println(utilDate.getClass().getName());
        System.out.println(sqlDate.getClass().getName());
    }
}
