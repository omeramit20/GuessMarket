package engine.core;

import engine.dto.EventDTO;
import engine.dto.TradeReceiptDTO;

import java.util.Map;

public interface IGuessMarketEngine {
    void loadEventsFromFile(String filePath) throws Exception;

    Map<Integer, EventDTO> getAllEvents();
    Map<String, engine.dto.UserDTO> getAllUsers();
    EventDTO getEventById(int eventId);

    TradeReceiptDTO buyShares(String username, int eventId, int optionIndex, int sharesToBuy);
    void closeEvent(int eventId, int winningOptionIndex);
    void saveSystemState(String filePath);
    void loadSystemState(String filePath);

    java.util.List<engine.dto.UserHoldingDTO> getUserHoldings(String username);
    java.util.List<engine.dto.UserEventParticipationDTO> getUserEventParticipation(String username);
    java.util.List<Double> getUserBalanceHistory(String username);
    void executeOrderBookOrder(String username, int eventId, String optionName,
                               engine.models.Order.OrderType orderType, double price, int quantity);
    void openEvent(int eventId, String username);

    int createLmsrEvent(String creatorUsername, String name, String description,
                         int commission, engine.models.CommissionType commissionType,
                         String option1, String option2, int b);

    int createOrderBookEvent(String creatorUsername, String name, String description,
                              int commission, engine.models.CommissionType commissionType,
                              String option1, String option2, int initial, int d, boolean allowMint);
}