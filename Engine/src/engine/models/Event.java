package engine.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event implements Serializable {
    private final int id;
    private final String name;
    private final String description;
    private final int commission;
    private final CommissionType commissionType;
    private final int b;
    private EventStatus status;
    private Option winningOption; // when event ends
    private final List<Option> options;
    private final List<TradeRecord> tradeHistory;
    private double totalCommissionCollected;
    private double accountBalance;
    private final boolean isLMSR;
    private final java.util.List<Order> buyOrders = new java.util.ArrayList<>();
    private final java.util.List<Order> sellOrders = new java.util.ArrayList<>();
    private String marketMaker;
    private boolean allowMint;
    private double initialPrice;
    private int dValue;


    public Event(int id, String name, String description, int commission, CommissionType commissionType, int b, boolean isLMSR) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commission = commission;
        this.commissionType = commissionType;
        this.b = b;
        this.status = EventStatus.NOT_ACTIVE;
        this.options = new ArrayList<>();
        this.tradeHistory = new ArrayList<>();
        this.totalCommissionCollected = 0.0;
        this.accountBalance = b * Math.log(2);
        this.isLMSR = isLMSR;
    }

    public void addOption(Option option) {
        this.options.add(option);
    }

    public int getId() {
        return id;
    }

    public List<TradeRecord> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    public void addCommission(double amount) {
        this.totalCommissionCollected += amount;
        this.accountBalance += amount;
    }

    public void addTradeRecord(TradeRecord record) {
        this.tradeHistory.add(record);
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCommission() {
        return commission;

    }
    public CommissionType getCommissionType() {
        return commissionType;

    }

    public EventStatus getStatus() {
        return status;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public int getB() {
        return b;
    }

    public void setStatus(EventStatus status) { this.status = status; }

    public boolean isClosed() {
        return this.status == EventStatus.CLOSED;
    }

    public void closeEvent(Option winningOption) {
        if (this.isClosed()) {
            throw new IllegalStateException("Event is already closed.");
        }
        this.status = EventStatus.CLOSED;
        this.winningOption = winningOption;
    }

    public Option getWinningOption() {
        return winningOption;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void payWinners(double amount) {
        this.accountBalance -= amount;
    }

    public void addFundsToAccount(double amount) {
        this.accountBalance += amount;
    }

    public void registerRetainedCommission(double amount) {
        this.totalCommissionCollected += amount;
    }

    public java.util.List<Order> getBuyOrders() {
        return buyOrders;
    }

    public java.util.List<Order> getSellOrders() {
        return sellOrders;
    }

    public void addBuyOrder(Order order) {
        buyOrders.add(order);
        // מיון פקודות קנייה: מהמחיר הגבוה לנמוך (הקונה שמוכן לשלם הכי הרבה עדיף ראשון)
        buyOrders.sort((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
    }

    public void addSellOrder(Order order) {
        sellOrders.add(order);
        // מיון פקודות מכיר: מהמחיר הנמוך לגבוה (המוכר שדורש הכי מעט עדיף ראשון)
        sellOrders.sort((o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));
    }

    public boolean isLMSR() { return isLMSR; }

    public void setOrderBookParams(String marketMaker, boolean allowMint, double initialPrice, int dValue) {
        this.marketMaker = marketMaker;
        this.allowMint = allowMint;
        this.initialPrice = initialPrice;
        this.dValue = dValue;
    }

    public String getMarketMaker() { return marketMaker; }
    public boolean isAllowMint() { return allowMint; }
    public double getInitialPrice() { return initialPrice; }
    public int getDValue() { return dValue; }
}