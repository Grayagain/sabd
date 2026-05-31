package lab3.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;

@Service
public class ClientCertificateService {

    public X509Certificate extractClientCertificate(HttpServletRequest request) {
        Object jakartaCertificates = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (jakartaCertificates instanceof X509Certificate[] certificates && certificates.length > 0) {
            return certificates[0];
        }

        Object javaxCertificates = request.getAttribute("javax.servlet.request.X509Certificate");
        if (javaxCertificates instanceof X509Certificate[] certificates && certificates.length > 0) {
            return certificates[0];
        }

        return null;
    }
}
