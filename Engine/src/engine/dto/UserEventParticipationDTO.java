package engine.dto;

import java.util.List;

public class UserEventParticipationDTO {
    private final int eventId;
    private final String eventName;
    private final String eventType; // "LMSR" or "OrderBook"
    private final String eventStatus;
    private final List<TradeDTO> tradeHistory; // LMSR only, newest first
    private final List<OptionParticipationDTO> optionBreakdown; // OrderBook only
    private final Double profitLoss; // OrderBook only; null unless the event is closed
    private final double totalCommissionPaid; // OrderBook only

    public UserEventParticipationDTO(int eventId, String eventName, String eventType, String eventStatus,
                                      List<TradeDTO> tradeHistory, List<OptionParticipationDTO> optionBreakdown,
                                      Double profitLoss, double totalCommissionPaid) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventStatus = eventStatus;
        this.tradeHistory = tradeHistory;
        this.optionBreakdown = optionBreakdown;
        this.profitLoss = profitLoss;
        this.totalCommissionPaid = totalCommissionPaid;
    }

    public int getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getEventType() { return eventType; }
    public String getEventStatus() { return eventStatus; }
    public List<TradeDTO> getTradeHistory() { return tradeHistory; }
    public List<OptionParticipationDTO> getOptionBreakdown() { return optionBreakdown; }
    public Double getProfitLoss() { return profitLoss; }
    public double getTotalCommissionPaid() { return totalCommissionPaid; }
}
