package lab4.model;

public class OrderDto {

    private final Long id;
    private final String number;
    private final Double amount;

    public OrderDto(Long id, String number, Double amount) {
        this.id = id;
        this.number = number;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Double getAmount() {
        return amount;
    }
}
