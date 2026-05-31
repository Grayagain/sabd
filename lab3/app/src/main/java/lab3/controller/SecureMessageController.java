package lab3.controller;

import jakarta.servlet.http.HttpServletRequest;
import lab3.model.MessageRequest;
import lab3.model.MessageResponse;
import lab3.service.ClientCertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/api/messages")
public class SecureMessageController {

    private final ClientCertificateService clientCertificateService;

    public SecureMessageController(ClientCertificateService clientCertificateService) {
        this.clientCertificateService = clientCertificateService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> acceptMessage(
            @RequestBody MessageRequest request,
            HttpServletRequest servletRequest,
            Principal principal
    ) {
        X509Certificate certificate = clientCertificateService.extractClientCertificate(servletRequest);
        String clientSubject = certificate == null ? "unavailable" : certificate.getSubjectX500Principal().getName();
        String clientPrincipal = principal == null ? clientSubject : principal.getName();

        MessageResponse response = new MessageResponse(
                "ACCEPTED",
                request.message(),
                clientPrincipal,
                clientSubject
        );

        return ResponseEntity.ok(response);
    }
}
