package lab4.controller;

import lab4.model.OrderDto;
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
@RequestMapping("/backend/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private static final List<OrderDto> ORDERS = Arrays.asList(
            new OrderDto(1L, "ORD-001", 120.50),
            new OrderDto(2L, "ORD-002", 89.90)
    );

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders() {
        logger.info("order-service handled list request");
        return ResponseEntity.ok(serviceResponse(ORDERS));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        logger.info("order-service handled item request for id={}", id);
        return ORDERS.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst()
                .map(order -> ResponseEntity.ok(serviceResponse(order)))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
    }

    private Map<String, Object> serviceResponse(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "order-service");
        response.put("data", data);
        return response;
    }
}
