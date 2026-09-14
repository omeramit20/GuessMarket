package engine.dto;

public class UserHoldingDTO {
    private final String username;
    private final String eventName;
    private final String optionName;
    private final int quantity;

    public UserHoldingDTO(String username, String eventName, String optionName, int quantity) {
        this.username = username;
        this.eventName = eventName;
        this.optionName = optionName;
        this.quantity = quantity;
    }

    public String getUsername() { return username; }
    public String getEventName() { return eventName; }
    public String getOptionName() { return optionName; }
    public int getQuantity() { return quantity; }
}