package lab3.model;

public record MessageResponse(
        String status,
        String echo,
        String clientPrincipal,
        String clientSubject
) {
}
