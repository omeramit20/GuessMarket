package engine.dto;

public class OptionParticipationDTO {
    private final String optionName;
    private final int quantity;
    private final double amountPaid;
    private final double commissionPaid;

    public OptionParticipationDTO(String optionName, int quantity, double amountPaid, double commissionPaid) {
        this.optionName = optionName;
        this.quantity = quantity;
        this.amountPaid = amountPaid;
        this.commissionPaid = commissionPaid;
    }

    public String getOptionName() { return optionName; }
    public int getQuantity() { return quantity; }
    public double getAmountPaid() { return amountPaid; }
    public double getCommissionPaid() { return commissionPaid; }
}
