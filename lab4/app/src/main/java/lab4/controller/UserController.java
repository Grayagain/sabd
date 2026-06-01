package lab4.controller;

import lab4.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/backend/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final List<UserDto> USERS = Arrays.asList(
            new UserDto(1L, "Alice", "alice@example.com"),
            new UserDto(2L, "Bob", "bob@example.com")
    );

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers() {
        logger.info("user-service handled list request");
        return ResponseEntity.ok(serviceResponse(USERS));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        logger.info("user-service handled item request for id={}", id);
        return USERS.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .map(user -> ResponseEntity.ok(serviceResponse(user)))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    private Map<String, Object> serviceResponse(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "user-service");
        response.put("data", data);
        return response;
    }
}
