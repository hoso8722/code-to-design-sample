package exam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

// 表示される内容も、手順0と変わっていないことを確かめる
class LogTest {
    static String run(String className) throws Exception {
        Class<?> c = Class.forName("exam." + className);
        c.getField("verbose").setBoolean(null, true);
        Method judge = c.getMethod("judge", String.class, int.class, int.class, int.class, String.class);
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, "UTF-8"));
        try {
            for (Cases.Case cs : Cases.ALL) {
                judge.invoke(null, cs.name(), cs.math(), cs.english(), cs.science(), cs.type());
            }
        } finally {
            System.setOut(original);
            c.getField("verbose").setBoolean(null, false);
        }
        return buffer.toString("UTF-8");
    }

    @Test
    void 手順1から6の表示は手順0と同じ() throws Exception {
        String expected = run("Step0");
        for (int i = 1; i <= 6; i++) {
            assertEquals(expected, run("Step" + i), "手順" + i);
        }
    }
}
