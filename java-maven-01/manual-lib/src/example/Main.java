// 実行: javac -cp lib/commons-lang3-3.21.0.jar -d out src/example/*.java
package example;

import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        System.out.println(StringUtils.capitalize("maven") + " " + StringUtils.reverse("abc"));
    }
}
