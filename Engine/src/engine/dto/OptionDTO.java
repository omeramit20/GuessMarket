package engine.dto;

public class OptionDTO {
    private final String name;
    private final int sharesBought;
    private final double currentPrice;

    public OptionDTO(String name, int sharesBought, double currentPrice) {
        this.name = name;
        this.sharesBought = sharesBought;
        this.currentPrice = currentPrice;
    }

    public String getName() { return name; }
    public int getSharesBought() { return sharesBought; }
    public double getCurrentPrice() { return currentPrice; }
}