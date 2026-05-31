package lab3.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab3.config.MutualTlsClientProperties;
import lab3.model.MessageRequest;
import lab3.model.MessageResponse;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class MutualTlsClient {

    private final MutualTlsClientProperties properties;
    private final SslContextFactory sslContextFactory;
    private final ObjectMapper objectMapper;

    public MutualTlsClient(
            MutualTlsClientProperties properties,
            SslContextFactory sslContextFactory,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.sslContextFactory = sslContextFactory;
        this.objectMapper = objectMapper;
    }

    public MessageResponse send(String message) {
        return send(message, properties.getBaseUrl());
    }

    public MessageResponse send(String message, String url) {
        SSLContext sslContext = sslContextFactory.createMutualTlsContext(
                properties.getKeyStore(),
                properties.getKeyStorePassword(),
                properties.getTrustStore(),
                properties.getTrustStorePassword()
        );
        return doSend(buildClient(sslContext), message, url);
    }

    public HttpResponse<String> sendWithoutCertificate(String message, String url) {
        SSLContext sslContext = sslContextFactory.createTrustOnlyContext(
                properties.getTrustStore(),
                properties.getTrustStorePassword()
        );
        return doSendRaw(buildClient(sslContext), message, url);
    }

    private HttpClient buildClient(SSLContext sslContext) {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

    private MessageResponse doSend(HttpClient client, String message, String url) {
        try {
            HttpResponse<String> response = doSendRaw(client, message, url);
            return objectMapper.readValue(response.body(), MessageResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to send mutual TLS request", exception);
        }
    }

    private HttpResponse<String> doSendRaw(HttpClient client, String message, String url) {
        try {
            String body = objectMapper.writeValueAsString(new MessageRequest(message));
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(properties.getReadTimeout())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to execute HTTPS request", exception);
        }
    }
}
