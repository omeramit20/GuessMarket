package engine.core;

import engine.dto.EventDTO;
import engine.dto.OptionDTO;
import engine.dto.TradeDTO;
import engine.dto.TradeReceiptDTO;
import engine.models.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuessMarketEngine implements IGuessMarketEngine {

    private Map<Integer, Event> currentEvents;

    public GuessMarketEngine() {
        this.currentEvents = new HashMap<>();
    }

    // Options 1: Read XML file with JAXB
    public void loadEventsFromFile(String filePath) {
        java.io.File file = new java.io.File(filePath.trim());

        // Check if file exists and is XML
        if (!file.exists()) {
            throw new engine.exception.InvalidFileException("Error: The file does not exist at the given path.");
        }
        if (!file.getName().toLowerCase().endsWith(".xml")) {
            throw new engine.exception.InvalidFileException("Error: The file must be an XML file (ending with .xml).");
        }

        // JAXB read file
        try {
            jakarta.xml.bind.JAXBContext jaxbContext = jakarta.xml.bind.JAXBContext.newInstance(engine.jaxb.GuessMarket.class);
            jakarta.xml.bind.Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            engine.jaxb.GuessMarket xmlData = (engine.jaxb.GuessMarket) unmarshaller.unmarshal(file);

            Map<Integer, Event> newEvents = new HashMap<>();

            // Check each event in file
            for (engine.jaxb.GMEvent eventXML : xmlData.getGMEvents().getGMEvent()) {

                if (newEvents.containsKey(eventXML.getId())) {
                    throw new engine.exception.DuplicateEventIdException(eventXML.getId());
                }

                String eventName = String.join(" ", eventXML.getName());

                int commissionValue = eventXML.getComision().getValue();

                if (commissionValue < 0 || commissionValue > 90) {
                    throw new engine.exception.InvalidCommissionException(eventName, commissionValue);
                }

                engine.models.CommissionType type = eventXML.getComision().getType().equalsIgnoreCase("on-close")
                        ? engine.models.CommissionType.ON_CLOSE
                        : engine.models.CommissionType.ON_PURCHASE;

                Event newEvent = new Event(
                        eventXML.getId(),
                        eventName,
                        eventXML.getDescription(),
                        commissionValue,
                        type,
                        eventXML.getGMMethod().getGMLMSR().getB()
                );

                for (String optionName : eventXML.getGMOptions().getGMOption()) {
                    newEvent.addOption(new engine.models.Option(optionName));
                }

                if (newEvent.getOptions().size() < 2) {
                    throw new engine.exception.GuessMarketException("Error: Event '" + newEvent.getName() + "' must have at least 2 options for LMSR to work.");
                }

                newEvents.put(newEvent.getId(), newEvent);
            }

            this.currentEvents = newEvents;

        } catch (jakarta.xml.bind.JAXBException e) {
            throw new engine.exception.InvalidFileException("Error: Failed to parse the XML file. Please ensure it matches the required schema.");
        }
    }

    @Override
    public Map<Integer, EventDTO> getAllEvents() {
        Map<Integer, EventDTO> dtoMap = new HashMap<>();
        for (Event event : currentEvents.values()) {
            dtoMap.put(event.getId(), createEventDTO(event));
        }
        return dtoMap;
    }

    @Override
    public EventDTO getEventById(int eventId) {
        Event event = currentEvents.get(eventId);
        return event != null ? createEventDTO(event) : null;
    }

    @Override
    public TradeReceiptDTO buyShares(int eventId, int optionIndex, int sharesToBuy) {
        Event event = currentEvents.get(eventId);
        if (event == null) {
            throw new engine.exception.GuessMarketException("Error: Event with ID " + eventId + " not found.");
        }
        if (event.isClosed()) {
            throw new engine.exception.GuessMarketException("Error: Event is closed.");
        }

        if (sharesToBuy <= 0) {
            throw new engine.exception.GuessMarketException("Error: You must buy at least 1 share.");
        }

        List<engine.models.Option> options = event.getOptions();
        if (optionIndex < 0 || optionIndex >= options.size()) {
            throw new engine.exception.GuessMarketException("Error: Invalid option selected.");
        }

        double b = event.getB();

        // calculate sum before purchase
        double sumBefore = 0.0;
        for (engine.models.Option opt : options) {
            sumBefore += Math.exp(opt.getSharesBought() / b);
        }
        double costBefore = b * Math.log(sumBefore);

        // calculate sum after purchase
        double sumAfter = 0.0;
        for (int i = 0; i < options.size(); i++) {
            int q = options.get(i).getSharesBought();
            // add shares to the user's selected option
            if (i == optionIndex) {
                q += sharesToBuy;
            }
            sumAfter += Math.exp(q / b);
        }
        double costAfter = b * Math.log(sumAfter);

        // total trade cost
        double tradeCost = costAfter - costBefore;
        event.addFundsToAccount(tradeCost);

        // calculate commission if comission type is on_purchase
        double commissionAdded = 0.0;
        if (event.getCommissionType() == engine.models.CommissionType.ON_PURCHASE) {
            commissionAdded = tradeCost * (event.getCommission() / 100.0);
            event.addCommission(commissionAdded);
        }

        double totalPaid = tradeCost + commissionAdded;

        // update shares in event
        options.get(optionIndex).addShares(sharesToBuy);

        // save record in event's trade history
        engine.models.TradeRecord record = new engine.models.TradeRecord(
                options.get(optionIndex).getName(),
                sharesToBuy,
                totalPaid
        );
        event.addTradeRecord(record);

        return new TradeReceiptDTO(tradeCost, commissionAdded, totalPaid);
    }

    @Override
    public void closeEvent(int eventId, int winningOptionIndex) {
        Event event = currentEvents.get(eventId);
        if (event == null) {
            throw new engine.exception.GuessMarketException("Error: Event with ID " + eventId + " not found.");
        }

        if (event.isClosed()) {
            throw new engine.exception.GuessMarketException("Error: This event is already closed.");
        }

        List<engine.models.Option> options = event.getOptions();

        if (winningOptionIndex < 0 || winningOptionIndex >= options.size()) {
            throw new engine.exception.GuessMarketException("Error: Invalid winning option selected.");
        }

        engine.models.Option winningOption = options.get(winningOptionIndex);
        double payouts = winningOption.getSharesBought();

        if (event.getCommissionType() == engine.models.CommissionType.ON_CLOSE) {
            double commissionCollected = payouts * (event.getCommission() / 100.0);
            event.registerRetainedCommission(commissionCollected);
            payouts -= commissionCollected;
        }

        event.payWinners(payouts);
        event.closeEvent(winningOption);
    }

    // Save system state to binary file using seriallization
    @Override
    public void saveSystemState(String filePath) {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(filePath))) {
            oos.writeObject(this.currentEvents);
        } catch (java.io.IOException e) {
            throw new engine.exception.GuessMarketException("Error saving system state: " + e.getMessage());
        }
    }

    // Load system from binary file
    @Override
    @SuppressWarnings("unchecked")
    public void loadSystemState(String filePath) {
        java.io.File file = new java.io.File(filePath.trim());
        if (!file.exists()) {
            throw new engine.exception.InvalidFileException("Error: State file not found.");
        }

        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            // replace all current events
            this.currentEvents = (Map<Integer, Event>) ois.readObject();
        } catch (java.io.IOException | ClassNotFoundException e) {
            throw new engine.exception.GuessMarketException("Error loading system state: The file might be corrupted or incompatible.");
        }
    }

    private EventDTO createEventDTO(Event event) {
        double b = event.getB();
        double denominator = 0.0;

        for (engine.models.Option opt : event.getOptions()) {
            denominator += Math.exp(opt.getSharesBought() / b);
        }

        List<OptionDTO> optionDTOs = new ArrayList<>();
        for (engine.models.Option opt : event.getOptions()) {
            double currentPrice = Math.exp(opt.getSharesBought() / b) / denominator;
            optionDTOs.add(new OptionDTO(opt.getName(), opt.getSharesBought(), currentPrice));
        }

        List<TradeDTO> tradeHistoryDTOs = new ArrayList<>();
        List<engine.models.TradeRecord> originalHistory = event.getTradeHistory();
        for (int i = originalHistory.size() - 1; i >= 0; i--) {
            engine.models.TradeRecord record = originalHistory.get(i);
            tradeHistoryDTOs.add(new TradeDTO(
                    record.getOptionName(),
                    record.getSharesQuantity(),
                    record.getPricePaid()
            ));
        }

        return new EventDTO(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCommission(),
                event.getCommissionType().toString(),
                event.getStatus().toString(),
                optionDTOs,
                event.getTotalCommissionCollected(),
                tradeHistoryDTOs,
                event.getWinningOption() != null ? event.getWinningOption().getName() : null,
                event.getAccountBalance()
        );
    }
}