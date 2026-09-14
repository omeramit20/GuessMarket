package engine.dto;

public class TradeDTO {
    private final String optionName;
    private final int sharesQuantity;
    private final double pricePaid;
    private final double commissionPaid;

    public TradeDTO(String optionName, int sharesQuantity, double pricePaid) {
        this(optionName, sharesQuantity, pricePaid, 0.0);
    }

    public TradeDTO(String optionName, int sharesQuantity, double pricePaid, double commissionPaid) {
        this.optionName = optionName;
        this.sharesQuantity = sharesQuantity;
        this.pricePaid = pricePaid;
        this.commissionPaid = commissionPaid;
    }

    public String getOptionName() { return optionName; }
    public int getSharesQuantity() { return sharesQuantity; }
    public double getPricePaid() { return pricePaid; }
    public double getCommissionPaid() { return commissionPaid; }
}