package lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ObfuscatorTest {

    @Test
    void testRoundTrip() {
        String original = "ABC123XYZ";

        String obf = Obfuscator.obfuscate(original);
        String back = Obfuscator.deobfuscate(obf);

        assertEquals(original, back);
    }
}