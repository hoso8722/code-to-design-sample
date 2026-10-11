// 実行: mvn -q package
package example;

import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        System.out.println(StringUtils.capitalize("maven") + " " + StringUtils.reverse("abc"));
    }
}
