package engine.dto;

public class TradeDTO {
    private final String optionName;
    private final int sharesQuantity;
    private final double pricePaid;

    public TradeDTO(String optionName, int sharesQuantity, double pricePaid) {
        this.optionName = optionName;
        this.sharesQuantity = sharesQuantity;
        this.pricePaid = pricePaid;
    }

    public String getOptionName() { return optionName; }
    public int getSharesQuantity() { return sharesQuantity; }
    public double getPricePaid() { return pricePaid; }
}