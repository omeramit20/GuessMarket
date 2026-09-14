package engine.models;

import java.io.Serializable;

public class TradeRecord implements Serializable {
    private final String username;
    private final String optionName;
    private final int sharesQuantity;
    private final double pricePaid;
    private final double commissionPaid;

    public TradeRecord(String username, String optionName, int shares, double pricePaid, double commissionPaid) {
        this.username = username;
        this.optionName = optionName;
        this.sharesQuantity = shares;
        this.pricePaid = pricePaid;
        this.commissionPaid = commissionPaid;
    }

    public String getUsername() { return username; }
    public String getOptionName() { return optionName; }
    public int getSharesQuantity() { return sharesQuantity; }
    public double getPricePaid() { return pricePaid; }
    public double getCommissionPaid() { return commissionPaid; }
}