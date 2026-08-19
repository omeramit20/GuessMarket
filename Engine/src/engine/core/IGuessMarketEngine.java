package engine.core;

import engine.dto.EventDTO;
import engine.dto.TradeReceiptDTO;

import java.util.Map;

public interface IGuessMarketEngine {
    void loadEventsFromFile(String filePath) throws Exception;

    Map<Integer, EventDTO> getAllEvents();
    EventDTO getEventById(int eventId);

    TradeReceiptDTO buyShares(int eventId, int optionIndex, int sharesToBuy);
    void closeEvent(int eventId, int winningOptionIndex);
    void saveSystemState(String filePath);
    void loadSystemState(String filePath);
}