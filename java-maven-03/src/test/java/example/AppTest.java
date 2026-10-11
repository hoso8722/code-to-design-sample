package example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void テストでは_JUnitのクラスを使える() {
        assertNotNull(new App());
    }
}
