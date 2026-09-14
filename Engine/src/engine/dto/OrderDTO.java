package engine.dto;

public class OrderDTO {
    private final String username;
    private final String optionName;
    private final int remainingQuantity;
    private final double price;

    public OrderDTO(String username, String optionName, int remainingQuantity, double price) {
        this.username = username;
        this.optionName = optionName;
        this.remainingQuantity = remainingQuantity;
        this.price = price;
    }

    public String getUsername() { return username; }
    public String getOptionName() { return optionName; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public double getPrice() { return price; }
}