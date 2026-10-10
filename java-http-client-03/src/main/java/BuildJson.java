// 実行: mvn -q compile exec:java -Dexec.mainClass=BuildJson
import com.fasterxml.jackson.databind.ObjectMapper;

public class BuildJson {
    record Order(String note, int quantity) {}

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String note = "\"急ぎ\"でお願いします\n2階の事務所へ";

        String concatenated = "{\"note\":\"" + note + "\",\"quantity\":1}";
        System.out.println("文字列連結:");
        System.out.println(concatenated);
        try {
            mapper.readTree(concatenated);
            System.out.println("  -> 読めた");
        } catch (Exception e) {
            System.out.println("  -> " + e.getClass().getSimpleName());
        }

        String written = mapper.writeValueAsString(new Order(note, 1));
        System.out.println("ObjectMapperで書き出し:");
        System.out.println(written);
        System.out.println("  -> 読み戻した値が元と同じか: " + mapper.readValue(written, Order.class).note().equals(note));
    }
}
