package engine.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private double balance;
    private Map<Integer, Map<String, Integer>> holdings;
    private Map<Integer, Map<String, Double>> netInvested;
    private Map<Integer, Map<String, Double>> commissionPaid;
    private final List<Double> balanceHistory;
    private boolean isBlocked;

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.holdings = new HashMap<>();
        this.netInvested = new HashMap<>();
        this.commissionPaid = new HashMap<>();
        this.balanceHistory = new ArrayList<>();
        this.balanceHistory.add(balance);
        this.isBlocked = false;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void addFunds(double amount) {
        this.balance += amount;
        this.balanceHistory.add(this.balance);
    }

    public void deductFunds(double amount) {
        this.balance -= amount;
        this.balanceHistory.add(this.balance);
    }

    public List<Double> getBalanceHistory() {
        return Collections.unmodifiableList(balanceHistory);
    }

    public void addHolding(int eventId, String optionName, int quantity) {
        this.holdings.putIfAbsent(eventId, new HashMap<>());
        Map<String, Integer> eventHoldings = this.holdings.get(eventId);
        eventHoldings.put(optionName, eventHoldings.getOrDefault(optionName, 0) + quantity);
    }

    public Map<Integer, Map<String, Integer>> getHoldings() {
        return holdings;
    }

    // Tracks net dollars paid (buys) vs. received (sells) per event/option, used to
    // derive "amount paid" and profit/loss for a user's Order Book participation.
    public void addInvestment(int eventId, String optionName, double amount) {
        this.netInvested.putIfAbsent(eventId, new HashMap<>());
        Map<String, Double> eventInvested = this.netInvested.get(eventId);
        eventInvested.put(optionName, eventInvested.getOrDefault(optionName, 0.0) + amount);
    }

    public Map<Integer, Map<String, Double>> getNetInvested() {
        return netInvested;
    }

    public void addCommissionPaid(int eventId, String optionName, double amount) {
        this.commissionPaid.putIfAbsent(eventId, new HashMap<>());
        Map<String, Double> eventCommission = this.commissionPaid.get(eventId);
        eventCommission.put(optionName, eventCommission.getOrDefault(optionName, 0.0) + amount);
    }

    public Map<Integer, Map<String, Double>> getCommissionPaid() {
        return commissionPaid;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }
}