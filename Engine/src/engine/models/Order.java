package engine.models;

public class Order {
    public enum OrderType {
        BUY, SELL
    }

    private static int idCounter = 1;

    private final int id;
    private final String username;
    private final int eventId;
    private final String optionName;
    private final OrderType type;
    private final double price;
    private final int originalQuantity;
    private int remainingQuantity;

    public Order(String username, int eventId, String optionName, OrderType type, double price, int quantity) {
        this.id = idCounter++;
        this.username = username;
        this.eventId = eventId;
        this.optionName = optionName;
        this.type = type;
        this.price = price;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public int getEventId() { return eventId; }
    public String getOptionName() { return optionName; }
    public OrderType getType() { return type; }
    public double getPrice() { return price; }
    public int getOriginalQuantity() { return originalQuantity; }
    public int getRemainingQuantity() { return remainingQuantity; }

    public void reduceQuantity(int amount) {
        this.remainingQuantity -= amount;
        if (this.remainingQuantity < 0) {
            this.remainingQuantity = 0;
        }
    }

    public boolean isCompleted() {
        return remainingQuantity <= 0;
    }
}