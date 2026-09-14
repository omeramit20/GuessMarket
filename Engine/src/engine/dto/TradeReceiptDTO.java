package engine.dto;

public class TradeReceiptDTO {
    public final double sharesCost;
    public final double commission;
    public final double totalPaid;

    public TradeReceiptDTO(double sharesCost, double commission, double totalPaid) {
        this.sharesCost = sharesCost;
        this.commission = commission;
        this.totalPaid = totalPaid;
    }

    public double getSharesCost() {
        return sharesCost;
    }

    public double getCommission() {
        return commission;
    }

    public double getTotalPaid() {
        return totalPaid;
    }
}