// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.Xxe
package example;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

public class Xxe {
    static String xml(Path secret) {
        // 外部実体 xxe を、ローカルのファイルとして定義し、本文で参照するXML
        return """
                <?xml version="1.0"?>
                <!DOCTYPE order [ <!ENTITY xxe SYSTEM "%s"> ]>
                <order><memo>&xxe;</memo></order>
                """.formatted(secret.toUri());
    }

    static String parse(DocumentBuilderFactory factory, String xml) throws Exception {
        var doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return doc.getElementsByTagName("memo").item(0).getTextContent().strip();
    }

    static void attempt(String label, DocumentBuilderFactory factory, String xml) {
        try {
            System.out.println(label + " → 読めた: " + parse(factory, xml));
        } catch (Exception e) {
            String m = String.valueOf(e.getMessage()).lines().findFirst().orElse("");
            System.out.println(label + " → 拒否: " + e.getClass().getSimpleName() + ": " + m);
        }
    }

    public static void main(String[] args) throws Exception {
        Path secret = Files.createTempFile("xxe-secret", ".txt");
        Files.writeString(secret, "★ローカルの秘密のファイルの内容★");
        String xml = xml(secret);

        // 1. 既定のパーサー
        attempt("既定のパーサー                       ", DocumentBuilderFactory.newInstance(), xml);

        // 2. DOCTYPE自体を禁止する
        DocumentBuilderFactory noDoctype = DocumentBuilderFactory.newInstance();
        noDoctype.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        attempt("DOCTYPEを禁止した設定                ", noDoctype, xml);

        // 3. 外部のDTD・スキーマへのアクセスを禁止する
        DocumentBuilderFactory noExternal = DocumentBuilderFactory.newInstance();
        noExternal.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        noExternal.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        attempt("外部アクセスを禁止した設定(属性)     ", noExternal, xml);

        Files.delete(secret);
    }
}
