// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.JacksonTyping
package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public class JacksonTyping {
    static void attempt(String label, ObjectMapper mapper, String json) {
        System.out.println(label);
        try {
            // 受け取る型は Object。実際のクラスは、JSONの側が決める
            Object value = mapper.readValue(json, Object.class);
            System.out.println("    成功: 復元されたクラス = " + value.getClass().getName());
        } catch (Exception e) {
            String msg = e.getMessage();
            System.out.println("    失敗: " + e.getClass().getSimpleName() + ": " + msg.substring(0, Math.min(msg.length(), 110)) + " ...");
        }
    }

    public static void main(String[] args) {
        String gadget = "[\"example.Gadget\",{\"command\":\"echo hello\"}]";
        String note = "[\"example.safe.Note\",{\"text\":\"こんにちは\"}]";

        // 1. 型指定(defaultTyping)を、制限なしで有効にする(よくない例)
        PolymorphicTypeValidator permissive = BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build();
        ObjectMapper open = new ObjectMapper();
        open.activateDefaultTyping(permissive, ObjectMapper.DefaultTyping.NON_FINAL);

        // 2. 許可するパッケージを限る(改善した例)
        PolymorphicTypeValidator limited = BasicPolymorphicTypeValidator.builder().allowIfSubType("example.safe.").build();
        ObjectMapper restricted = new ObjectMapper();
        restricted.activateDefaultTyping(limited, ObjectMapper.DefaultTyping.NON_FINAL);

        // 3. 型指定を使わない(既定)
        ObjectMapper plain = new ObjectMapper();

        System.out.println("## 型指定を制限なしで有効にした ObjectMapper");
        attempt("  example.safe.Note を指定", open, note);
        attempt("  example.Gadget を指定", open, gadget);

        System.out.println("## 許可するパッケージを example.safe. に限った ObjectMapper");
        attempt("  example.safe.Note を指定", restricted, note);
        attempt("  example.Gadget を指定", restricted, gadget);

        System.out.println("## 型指定を使わない既定の ObjectMapper");
        attempt("  同じJSON(Gadget)", plain, gadget);
    }
}
