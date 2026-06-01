package lab4;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GatewayRoutingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void rejectsGatewayRequestWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/users"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Missing X-Auth-Token");
    }

    @Test
    void routesUsersRequestThroughGateway() {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/users/1"),
                HttpMethod.GET,
                authorizedRequest(),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("user-service");
        assertThat(response.getBody()).contains("Alice");
    }

    @Test
    void routesOrdersRequestThroughGateway() {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/orders"),
                HttpMethod.GET,
                authorizedRequest(),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("order-service");
        assertThat(response.getBody()).contains("ORD-001");
    }

    private HttpEntity<Void> authorizedRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", "secret-token");
        return new HttpEntity<>(headers);
    }

    private String url(String path) {
        return "http://localhost:8080" + path;
    }
}
