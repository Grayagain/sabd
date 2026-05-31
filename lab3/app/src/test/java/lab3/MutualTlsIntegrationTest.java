package lab3;

import lab3.client.MutualTlsClient;
import lab3.model.MessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MutualTlsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MutualTlsClient client;

    @Test
    void clientCanCallServerWithValidCertificate() {
        MessageResponse response = client.send("hello mtls", url());

        assertEquals("ACCEPTED", response.status());
        assertEquals("hello mtls", response.echo());
        assertTrue(response.clientSubject().contains("CN=lab3-client"));
    }

    @Test
    void serverResponseContainsClientIdentity() {
        MessageResponse response = client.send("identity check", url());

        assertTrue(response.clientPrincipal().contains("lab3-client"));
        assertTrue(response.clientSubject().contains("lab3-client"));
    }

    @Test
    void clientWithoutCertificateIsRejected() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> client.sendWithoutCertificate("missing cert", url())
        );

        Throwable cause = exception.getCause();
        assertTrue(cause != null);
    }

    private String url() {
        return "https://localhost:" + port + "/api/messages";
    }
}
