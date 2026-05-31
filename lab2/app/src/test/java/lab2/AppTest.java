package lab2;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {

    private static X509Certificate certificate;
    private static PrivateKey privateKey;

    @BeforeAll
    static void setUpCrypto() throws Exception {
        App.ensureCryptoSetup();
        certificate = App.loadCertificate(Paths.get("app", "public.cer").toString());
        privateKey = App.loadPrivateKey(Paths.get("app", "private.p12").toString(), "baeldung", "password");
    }

    @Test
    void encryptAndDecryptRoundTrip() throws Exception {
        byte[] original = "unique cms message".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = App.encryptData(original, certificate);
        byte[] decrypted = App.decryptData(encrypted, privateKey);

        assertArrayEquals(original, decrypted);
    }

    @Test
    void signAndVerifyRoundTrip() throws Exception {
        byte[] original = "signed cms message".getBytes(StandardCharsets.UTF_8);

        byte[] signed = App.signData(original, certificate, privateKey);

        assertTrue(App.verify(signed));
        assertArrayEquals(original, App.extractSignedContent(signed));
    }
}
