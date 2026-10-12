// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.HtmlEscaping
package example;

import org.springframework.web.util.HtmlUtils;

public class HtmlEscaping {
    // よくない例:< と > だけを置き換える
    static String naiveEscape(String input) {
        return input.replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void main(String[] args) {
        String script = "<script>alert('XSS')</script>";
        System.out.println("## HTMLの本文に出す");
        System.out.println("  そのまま:          <p>" + script + "</p>");
        System.out.println("  < と > だけ置換:   <p>" + naiveEscape(script) + "</p>");
        System.out.println("  HtmlUtils:         <p>" + HtmlUtils.htmlEscape(script) + "</p>");

        // 属性の値に出す場合:引用符を含む入力で、属性が増やされる
        String attr = "x\" onmouseover=\"alert(1)";
        System.out.println("## HTMLの属性の値に出す");
        System.out.println("  そのまま:          <input value=\"" + attr + "\">");
        System.out.println("  < と > だけ置換:   <input value=\"" + naiveEscape(attr) + "\">");
        System.out.println("  HtmlUtils:         <input value=\"" + HtmlUtils.htmlEscape(attr) + "\">");
    }
}
