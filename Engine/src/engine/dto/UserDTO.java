package engine.dto;

public class UserDTO {
    private final String name;
    private final double balance;

    public UserDTO(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }
}