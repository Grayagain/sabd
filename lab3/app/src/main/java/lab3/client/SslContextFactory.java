package lab3.client;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Component
public class SslContextFactory {

    private final ResourceLoader resourceLoader;

    public SslContextFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public SSLContext createMutualTlsContext(
            String keyStoreLocation,
            String keyStorePassword,
            String trustStoreLocation,
            String trustStorePassword
    ) {
        try {
            KeyStore keyStore = loadKeyStore(keyStoreLocation, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            KeyStore trustStore = loadKeyStore(trustStoreLocation, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create mutual TLS SSLContext", exception);
        }
    }

    public SSLContext createTrustOnlyContext(String trustStoreLocation, String trustStorePassword) {
        try {
            KeyStore trustStore = loadKeyStore(trustStoreLocation, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create trust-only SSLContext", exception);
        }
    }

    private KeyStore loadKeyStore(String location, String password) throws Exception {
        Resource resource = resourceLoader.getResource(location);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = resource.getInputStream()) {
            keyStore.load(inputStream, password.toCharArray());
        }

        return keyStore;
    }
}
