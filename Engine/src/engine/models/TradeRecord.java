package engine.models;

import java.io.Serializable;

public class TradeRecord implements Serializable {
    private final String optionName;
    private final int sharesQuantity;
    private final double pricePaid;

    public TradeRecord(String optionName, int shares, double pricePaid) {
        this.optionName = optionName;
        this.sharesQuantity = shares;
        this.pricePaid = pricePaid;
    }

    public String getOptionName() { return optionName; }
    public int getSharesQuantity() { return sharesQuantity; }
    public double getPricePaid() { return pricePaid; }
}