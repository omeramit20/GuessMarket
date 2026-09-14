package engine.dto;

public class OptionDTO {
    private final String name;
    private final int sharesBought;
    private final double currentPrice;
    private final double bid;
    private final double ask;
    private final double last;
    private final double mid;
    private final double spread;

    public OptionDTO(String name, int sharesBought, double currentPrice) {
        this(name, sharesBought, currentPrice, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public OptionDTO(String name, int sharesBought, double currentPrice, double bid, double ask, double last, double mid, double spread) {
        this.name = name;
        this.sharesBought = sharesBought;
        this.currentPrice = currentPrice;
        this.bid = bid;
        this.ask = ask;
        this.last = last;
        this.mid = mid;
        this.spread = spread;
    }

    public String getName() { return name; }
    public int getSharesBought() { return sharesBought; }
    public double getCurrentPrice() { return currentPrice; }
    public double getBid() { return bid; }
    public double getAsk() { return ask; }
    public double getLast() { return last; }
    public double getMid() { return mid; }
    public double getSpread() { return spread; }
}