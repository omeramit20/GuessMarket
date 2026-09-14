package engine.dto;

import java.util.List;

public class EventDTO {
    private final int id;
    private final String name;
    private final String description;
    private final int commission;
    private final String commissionType;
    private final String status;
    private final List<OptionDTO> options;
    private final double totalCommissionCollected;
    private final List<TradeDTO> tradeHistory;
    private final String winningOption;
    private final double accountBalance;
    private String type; // LMSR or Order Book
    private final String marketMaker;
    private final java.util.List<OrderDTO> buyOrders;
    private final java.util.List<OrderDTO> sellOrders;
    private final java.util.List<UserHoldingDTO> participantsHoldings;

    public EventDTO(int id, String name, String description, int commission,
                    String commissionType, String status, List<OptionDTO> options,
                    double totalCommissionCollected, List<TradeDTO> tradeHistory,
                    String winningOption, double accountBalance, String type,
                    String marketMaker, List<OrderDTO> buyOrders,
                    List<OrderDTO> sellOrders, List<UserHoldingDTO> participantsHoldings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commission = commission;
        this.commissionType = commissionType;
        this.status = status;
        this.options = options;
        this.totalCommissionCollected = totalCommissionCollected;
        this.tradeHistory = tradeHistory;
        this.winningOption = winningOption;
        this.accountBalance = accountBalance;
        this.type = type;
        this.marketMaker = marketMaker;
        this.buyOrders = buyOrders;
        this.sellOrders = sellOrders;
        this.participantsHoldings = participantsHoldings;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCommission() { return commission; }
    public String getCommissionType() { return commissionType; }
    public String getStatus() { return status; }
    public List<OptionDTO> getOptions() { return options; }
    public double getTotalCommissionCollected() { return totalCommissionCollected; }
    public List<TradeDTO> getTradeHistory() { return tradeHistory; }
    public String getWinningOption() { return winningOption; }
    public double getAccountBalance() { return accountBalance; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMarketMaker() { return marketMaker; }
    public List<OrderDTO> getBuyOrders() { return buyOrders; }
    public List<OrderDTO> getSellOrders() { return sellOrders; }
    public List<UserHoldingDTO> getParticipantsHoldings() { return participantsHoldings; }
}