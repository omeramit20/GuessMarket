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
    private Map<String, engine.models.User> currentUsers;

    public GuessMarketEngine() {
        this.currentEvents = new HashMap<>();
        this.currentUsers = new HashMap<>();
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
            Map<String, engine.models.User> newUsers = new HashMap<>();
            Map<Integer, String> eventToMM = new HashMap<>(); // שומר איזה אירוע מנוהל על ידי איזה MM

            // 1. קריאת משתמשים ו-Market Makers (תרגיל 2)
            if (xmlData.getGMUsers() != null) {
                for (engine.jaxb.GMUser userXML : xmlData.getGMUsers().getGMUser()) {
                    String username = userXML.getName().trim();

                    // שליפת היתרה לפי השם המדויק מהסכמה (initial-cash)
                    double balance = userXML.getInitialCash();

                    if (newUsers.containsKey(username)) {
                        throw new engine.exception.GuessMarketException("Duplicate user name found: " + username);
                    }
                    if (balance <= 0) {
                        throw new engine.exception.GuessMarketException("User balance must be greater than 0 for user: " + username);
                    }

                    newUsers.put(username, new engine.models.User(username, balance));

                    // טיפול ב-Market Maker שמכיל רשימה של אירועים
                    if (userXML.getGMMarketMaker() != null) {
                        // ריצה על כל תגיות ה-<event> של ה-Market Maker
                        for (engine.jaxb.Event mmEvent : userXML.getGMMarketMaker().getEvent()) {
                            int mmEventId = mmEvent.getId();

                            if (eventToMM.containsKey(mmEventId)) {
                                throw new engine.exception.GuessMarketException("Event ID " + mmEventId + " already has a Market Maker assigned.");
                            }
                            eventToMM.put(mmEventId, username);
                        }
                    }
                }
            }

            // 2. Check each event in file
            for (engine.jaxb.GMEvent eventXML : xmlData.getGMEvents().getGMEvent()) {

                if (newEvents.containsKey(eventXML.getId())) {
                    throw new engine.exception.DuplicateEventIdException(eventXML.getId());
                }

                // ולידציה של תרגיל 2: לכל אירוע חייב להיות Market Maker אחד בדיוק
                if (xmlData.getGMUsers() != null && !eventToMM.containsKey(eventXML.getId())) {
                    throw new engine.exception.GuessMarketException("Event ID " + eventXML.getId() + " does not have a Market Maker assigned.");
                }

                String eventName = String.join(" ", eventXML.getName());

                int commissionValue = eventXML.getCommission().getValue();

                if (commissionValue < 0 || commissionValue > 90) {
                    throw new engine.exception.InvalidCommissionException(eventName, commissionValue);
                }

                engine.models.CommissionType type = eventXML.getCommission().getType().equalsIgnoreCase("on-close")
                        ? engine.models.CommissionType.ON_CLOSE
                        : engine.models.CommissionType.ON_PURCHASE;

                boolean isLMSR = (eventXML.getGMMethod().getGMLMSR() != null);
                int bValue = isLMSR ? (int) eventXML.getGMMethod().getGMLMSR().getB() : 0;

                Event newEvent = new Event(
                        eventXML.getId(),
                        eventName,
                        eventXML.getDescription(),
                        commissionValue,
                        type,
                        bValue,
                        isLMSR
                );

                // --- חילוץ נתוני Order Book ו-Market Maker ---
                String mmName = eventToMM.get(eventXML.getId()); // שליפת שם ה-Market Maker של האירוע
                if (!isLMSR && eventXML.getGMMethod().getGMOrderBook() != null) {
                    var orderBookXML = eventXML.getGMMethod().getGMOrderBook();

                    // המרה ממחרוזת לבוליאן
                    boolean allowMint = Boolean.parseBoolean(orderBookXML.getAllowMint());

                    double initial = orderBookXML.getInitial();
                    int d = orderBookXML.getD();
                    newEvent.setOrderBookParams(mmName, allowMint, initial, d);
                } else {
                    // עבור LMSR פשוט נשמור מי ה-MM (אין allow-mint)
                    newEvent.setOrderBookParams(mmName, false, 0.0, 0);
                }

                for (String optionName : eventXML.getGMOptions().getGMOption()) {
                    newEvent.addOption(new engine.models.Option(optionName));
                }

                if (newEvent.getOptions().size() < 2) {
                    throw new engine.exception.GuessMarketException("Error: Event '" + newEvent.getName() + "' must have at least 2 options for LMSR to work.");
                }

                newEvents.put(newEvent.getId(), newEvent);
            }

            // ולידציה של תרגיל 2: MM לא יכול להפנות לאירוע שלא קיים בקובץ
            for (Integer mmEventId : eventToMM.keySet()) {
                if (!newEvents.containsKey(mmEventId)) {
                    throw new engine.exception.GuessMarketException(
                            "Error: Market Maker '" + eventToMM.get(mmEventId) + "' references event ID " + mmEventId + ", which does not exist in the file.");
                }
            }

            // עדכון המערכת רק אם כל הבדיקות עברו
            this.currentEvents = newEvents;
            this.currentUsers = newUsers;

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
    public Map<String, engine.dto.UserDTO> getAllUsers() {
        Map<String, engine.dto.UserDTO> dtoMap = new HashMap<>();
        for (engine.models.User user : currentUsers.values()) {
            dtoMap.put(user.getName(), new engine.dto.UserDTO(user.getName(), user.getBalance()));
        }
        return dtoMap;
    }

    @Override
    public EventDTO getEventById(int eventId) {
        Event event = currentEvents.get(eventId);
        return event != null ? createEventDTO(event) : null;
    }

    @Override
    public TradeReceiptDTO buyShares(String username, int eventId, int optionIndex, int sharesToBuy) {

        // --- תוספת לתרגיל 2: חילוץ ובדיקת המשתמש ---
        engine.models.User user = currentUsers.get(username);
        if (user == null) {
            throw new engine.exception.GuessMarketException("Error: User '" + username + "' not found.");
        }

        if (user.isBlocked()) {
            throw new engine.exception.GuessMarketException("Error: User '" + username + "' is blocked due to a negative balance.");
        }

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

        // calculate commission if commission type is on_purchase
        double commissionAdded = 0.0;
        if (event.getCommissionType() == engine.models.CommissionType.ON_PURCHASE) {
            commissionAdded = tradeCost * (event.getCommission() / 100.0);
        }

        double totalPaid = tradeCost + commissionAdded;

        // --- תוספת לתרגיל 2: סליקה מול יתרת המשתמש ועדכון אחזקות ---
        // deleted to allow getting in minus and getting blocked
//        if (user.getBalance() < totalPaid) {
//            throw new engine.exception.GuessMarketException(String.format(
//                    "Error: Insufficient funds. Trade costs $%.2f but %s only has $%.2f.",
//                    totalPaid, username, user.getBalance()));
//        }

        user.deductFunds(totalPaid);
        user.addHolding(eventId, options.get(optionIndex).getName(), sharesToBuy);

        // --- המשך הלוגיקה המקורית שלך ---

        event.addFundsToAccount(tradeCost);
        if (commissionAdded > 0) {
            event.addCommission(commissionAdded);
        }

        // update shares in event
        options.get(optionIndex).addShares(sharesToBuy);

        // save record in event's trade history
        engine.models.TradeRecord record = new engine.models.TradeRecord(
                username,
                options.get(optionIndex).getName(),
                sharesToBuy,
                tradeCost,
                commissionAdded
        );
        event.addTradeRecord(record);

        if (user.getBalance() < 0) {
            user.setBlocked(true);
        }

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

        // Total shares actually outstanding for the winning option, derived from real user
        // holdings rather than Option.getSharesBought() -- for Order Book events that counter
        // also increments on secondary-market resales of already-issued shares, so it can
        // overcount the true outstanding quantity.
        int totalWinningShares = 0;
        for (engine.models.User u : currentUsers.values()) {
            java.util.Map<String, Integer> eventHoldings = u.getHoldings().get(eventId);
            if (eventHoldings != null) {
                totalWinningShares += eventHoldings.getOrDefault(winningOption.getName(), 0);
            }
        }

        // Base value per winning share: $1 for LMSR, or the event's base value 'd' for Order Book
        double pricePerShare = event.isLMSR() ? 1.0 : event.getDValue();
        double payoutPerShare = pricePerShare;

        if (event.getCommissionType() == engine.models.CommissionType.ON_CLOSE) {
            double commissionPerShare = pricePerShare * (event.getCommission() / 100.0);
            payoutPerShare = pricePerShare - commissionPerShare;
            event.registerRetainedCommission(commissionPerShare * totalWinningShares);
        }

        // Actually credit every holder of the winning option with their share of the payout
        for (engine.models.User u : currentUsers.values()) {
            java.util.Map<String, Integer> eventHoldings = u.getHoldings().get(eventId);
            if (eventHoldings == null) continue;
            int qty = eventHoldings.getOrDefault(winningOption.getName(), 0);
            if (qty > 0) {
                u.addFunds(qty * payoutPerShare);
            }
        }

        // Empty the event account completely: winners already received their share above;
        // whatever remains (leftover LMSR subsidy and/or ON_CLOSE commission) returns to the MM.
        double eventBalanceBeforeClose = event.getAccountBalance();
        double remainderToMM = eventBalanceBeforeClose - (payoutPerShare * totalWinningShares);
        event.payWinners(eventBalanceBeforeClose);

        engine.models.User marketMaker = currentUsers.get(event.getMarketMaker());
        if (marketMaker != null && remainderToMM != 0) {
            marketMaker.addFunds(remainderToMM);
        }

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
        List<OptionDTO> optionDTOs = new ArrayList<>();

        // שלפנו את ההיסטוריה כבר כאן כדי שנוכל להשתמש בה גם לחישובי ה-Order Book
        List<engine.models.TradeRecord> originalHistory = event.getTradeHistory();

        if (event.isLMSR()) {
            double b = event.getB();
            double denominator = 0.0;

            for (engine.models.Option opt : event.getOptions()) {
                denominator += Math.exp(opt.getSharesBought() / b);
            }

            for (engine.models.Option opt : event.getOptions()) {
                double currentPrice = Math.exp(opt.getSharesBought() / b) / denominator;
                // ב-LMSR הסטטיסטיקות מתאפסות (0.0) כי הן לא רלוונטיות
                optionDTOs.add(new OptionDTO(opt.getName(), opt.getSharesBought(), currentPrice));
            }
        } else {
            // עבור Order Book - חישוב נתונים סטטיסטיים
            for (engine.models.Option opt : event.getOptions()) {
                double last = 0.0;
                // חיפוש עסקת LAST מהסוף להתחלה
                for (int i = originalHistory.size() - 1; i >= 0; i--) {
                    if (originalHistory.get(i).getOptionName().equals(opt.getName())) {
                        last = originalHistory.get(i).getPricePaid();
                        break;
                    }
                }

                double bid = 0.0;
                // חיפוש ה-BID (מחיר קנייה מקסימלי)
                for (engine.models.Order o : event.getBuyOrders()) {
                    if (o.getOptionName().equals(opt.getName())) {
                        bid = o.getPrice();
                        break; // הרשימה ממוינת מהגבוה לנמוך
                    }
                }

                double ask = 0.0;
                // חיפוש ה-ASK (מחיר מכירה מינימלי)
                for (engine.models.Order o : event.getSellOrders()) {
                    if (o.getOptionName().equals(opt.getName())) {
                        ask = o.getPrice();
                        break; // הרשימה ממוינת מהנמוך לגבוה
                    }
                }

                double mid = (bid > 0 && ask > 0) ? (bid + ask) / 2.0 : 0.0;
                double spread = (bid > 0 && ask > 0) ? (ask - bid) : 0.0;

                // הוספת האופציה עם כל הנתונים הסטטיסטיים החדשים
                optionDTOs.add(new OptionDTO(opt.getName(), opt.getSharesBought(), event.getInitialPrice(), bid, ask, last, mid, spread));
            }
        }

        List<TradeDTO> tradeHistoryDTOs = new ArrayList<>();
        for (int i = originalHistory.size() - 1; i >= 0; i--) {
            engine.models.TradeRecord record = originalHistory.get(i);
            tradeHistoryDTOs.add(new TradeDTO(
                    record.getOptionName(),
                    record.getSharesQuantity(),
                    record.getPricePaid(),
                    record.getCommissionPaid()
            ));
        }

        // --- איסוף נתוני Order Book ל-DTO ---
        List<engine.dto.OrderDTO> buyOrdersDTO = new ArrayList<>();
        List<engine.dto.OrderDTO> sellOrdersDTO = new ArrayList<>();

        if (!event.isLMSR()) {
            for (engine.models.Order o : event.getBuyOrders()) {
                buyOrdersDTO.add(new engine.dto.OrderDTO(o.getUsername(), o.getOptionName(), o.getRemainingQuantity(), o.getPrice()));
            }
            for (engine.models.Order o : event.getSellOrders()) {
                sellOrdersDTO.add(new engine.dto.OrderDTO(o.getUsername(), o.getOptionName(), o.getRemainingQuantity(), o.getPrice()));
            }
        }

        // --- איסוף רשימת משתתפים ואחזקותיהם באירוע ---
        List<engine.dto.UserHoldingDTO> participantsDTO = new ArrayList<>();
        for (engine.models.User u : currentUsers.values()) {
            if (u.getHoldings() != null && u.getHoldings().containsKey(event.getId())) {
                java.util.Map<String, Integer> userEventHoldings = u.getHoldings().get(event.getId());
                for (java.util.Map.Entry<String, Integer> entry : userEventHoldings.entrySet()) {
                    if (entry.getValue() != 0) { // מציג רק אם יש אחזקה פעילה (כולל שורט שלילי אם יש)
                        participantsDTO.add(new engine.dto.UserHoldingDTO(u.getName(), event.getName(), entry.getKey(), entry.getValue()));
                    }
                }
            }
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
                event.getAccountBalance(),
                event.isLMSR() ? "LMSR" : "OrderBook",
                event.getMarketMaker(),
                buyOrdersDTO,
                sellOrdersDTO,
                participantsDTO
        );
    }

    @Override
    public java.util.List<engine.dto.UserHoldingDTO> getUserHoldings(String username) {
        engine.models.User user = currentUsers.get(username);
        java.util.List<engine.dto.UserHoldingDTO> holdingsList = new java.util.ArrayList<>();

        if (user == null || user.getHoldings() == null) {
            return holdingsList;
        }

        for (java.util.Map.Entry<Integer, java.util.Map<String, Integer>> entry : user.getHoldings().entrySet()) {
            int eventId = entry.getKey();
            Event event = currentEvents.get(eventId);
            String eventName = (event != null) ? event.getName() : "Event #" + eventId;

            for (java.util.Map.Entry<String, Integer> optionEntry : entry.getValue().entrySet()) {
                holdingsList.add(new engine.dto.UserHoldingDTO(
                        user.getName(),
                        eventName,
                        optionEntry.getKey(),
                        optionEntry.getValue()
                ));
            }
        }

        return holdingsList;
    }

    @Override
    public java.util.List<engine.dto.UserEventParticipationDTO> getUserEventParticipation(String username) {
        engine.models.User user = currentUsers.get(username);
        java.util.List<engine.dto.UserEventParticipationDTO> result = new java.util.ArrayList<>();
        if (user == null) return result;

        for (Event event : currentEvents.values()) {
            if (!isUserActiveInEvent(user, event)) continue;

            String type = event.isLMSR() ? "LMSR" : "OrderBook";
            java.util.List<engine.dto.TradeDTO> trades = null;
            java.util.List<engine.dto.OptionParticipationDTO> breakdown = null;
            Double profitLoss = null;
            double totalCommissionPaid = 0.0;

            if (event.isLMSR()) {
                trades = new ArrayList<>();
                List<engine.models.TradeRecord> history = event.getTradeHistory();
                for (int i = history.size() - 1; i >= 0; i--) {
                    engine.models.TradeRecord r = history.get(i);
                    if (username.equals(r.getUsername())) {
                        trades.add(new engine.dto.TradeDTO(r.getOptionName(), r.getSharesQuantity(), r.getPricePaid(), r.getCommissionPaid()));
                    }
                }
            } else {
                breakdown = new ArrayList<>();
                java.util.Map<String, Integer> holdings = user.getHoldings().getOrDefault(event.getId(), java.util.Collections.emptyMap());
                java.util.Map<String, Double> invested = user.getNetInvested().getOrDefault(event.getId(), java.util.Collections.emptyMap());
                java.util.Map<String, Double> commission = user.getCommissionPaid().getOrDefault(event.getId(), java.util.Collections.emptyMap());

                for (engine.models.Option opt : event.getOptions()) {
                    int qty = holdings.getOrDefault(opt.getName(), 0);
                    double paid = invested.getOrDefault(opt.getName(), 0.0);
                    double optionCommission = commission.getOrDefault(opt.getName(), 0.0);
                    if (qty == 0 && paid == 0.0 && optionCommission == 0.0) continue;
                    breakdown.add(new engine.dto.OptionParticipationDTO(opt.getName(), qty, paid, optionCommission));
                }
                totalCommissionPaid = commission.values().stream().mapToDouble(Double::doubleValue).sum();

                if (event.isClosed()) {
                    double pricePerShare = event.getDValue();
                    if (event.getCommissionType() == engine.models.CommissionType.ON_CLOSE) {
                        pricePerShare *= (1 - event.getCommission() / 100.0);
                    }
                    int winningQty = holdings.getOrDefault(event.getWinningOption().getName(), 0);
                    double received = winningQty * pricePerShare;
                    double totalInvested = invested.values().stream().mapToDouble(Double::doubleValue).sum();
                    profitLoss = received - totalInvested - totalCommissionPaid;
                }
            }

            result.add(new engine.dto.UserEventParticipationDTO(
                    event.getId(), event.getName(), type, event.getStatus().toString(),
                    trades, breakdown, profitLoss, totalCommissionPaid
            ));
        }
        return result;
    }

    @Override
    public java.util.List<Double> getUserBalanceHistory(String username) {
        engine.models.User user = currentUsers.get(username);
        if (user == null) return new ArrayList<>();
        return new ArrayList<>(user.getBalanceHistory());
    }

    private boolean isUserActiveInEvent(engine.models.User user, Event event) {
        if (user.getName().equals(event.getMarketMaker()) && event.getStatus() != engine.models.EventStatus.NOT_ACTIVE) {
            return true;
        }
        java.util.Map<String, Integer> eventHoldings = user.getHoldings().get(event.getId());
        if (eventHoldings != null && eventHoldings.values().stream().anyMatch(q -> q != 0)) {
            return true;
        }
        for (engine.models.Order o : event.getBuyOrders()) {
            if (o.getUsername().equals(user.getName())) return true;
        }
        for (engine.models.Order o : event.getSellOrders()) {
            if (o.getUsername().equals(user.getName())) return true;
        }
        return false;
    }

    public void executeOrderBookOrder(String username, int eventId, String optionName,
                                      engine.models.Order.OrderType orderType, double price, int quantity) {

        engine.models.User user = currentUsers.get(username);
        if (user == null) {
            throw new engine.exception.GuessMarketException("Error: User '" + username + "' not found.");
        }
        if (user.isBlocked()) {
            throw new engine.exception.GuessMarketException("Error: User '" + username + "' is blocked due to a negative balance.");
        }

        Event event = currentEvents.get(eventId);
        if (event == null) throw new engine.exception.GuessMarketException("Error: Event with ID " + eventId + " not found.");
        if (event.isClosed()) throw new engine.exception.GuessMarketException("Error: Event is closed.");
        if (quantity <= 0 || price <= 0) throw new engine.exception.GuessMarketException("Error: Invalid price or quantity.");

        // --- הוספת הגבלת מחיר מקסימלי לפי ערך הבסיס d ---
        double maxPrice = event.getDValue() - 0.01;
        if (price > maxPrice) {
            throw new engine.exception.GuessMarketException(
                    String.format("Error: Price cannot exceed $%.2f (Base value 'd' minus 0.01).", maxPrice)
            );
        }
        // ------------------------------------------------

        int remainingQtyToProcess = quantity;

        if (orderType == engine.models.Order.OrderType.BUY) {
            double totalCost = price * quantity;
            // deleted to let get into minus
//            if (user.getBalance() < totalCost) {
//                throw new engine.exception.GuessMarketException("Error: Insufficient funds for this order.");
//            }

            java.util.Iterator<engine.models.Order> sellIterator = event.getSellOrders().iterator();
            while (sellIterator.hasNext() && remainingQtyToProcess > 0) {
                engine.models.Order existingSell = sellIterator.next();

                if (existingSell.getOptionName().equals(optionName) && existingSell.getPrice() <= price) {
                    int matchQty = Math.min(remainingQtyToProcess, existingSell.getRemainingQuantity());

                    existingSell.reduceQuantity(matchQty);
                    remainingQtyToProcess -= matchQty;

                    // Commission is charged only to the buyer, at the moment of purchase
                    double matchAmount = existingSell.getPrice() * matchQty;
                    double commissionAmount = event.getCommissionType() == engine.models.CommissionType.ON_PURCHASE
                            ? matchAmount * (event.getCommission() / 100.0) : 0.0;

                    // 1. עדכון הקונה (המשתמש הנוכחי)
                    user.deductFunds(matchAmount + commissionAmount);
                    user.addHolding(eventId, optionName, matchQty);
                    user.addInvestment(eventId, optionName, matchAmount);
                    if (commissionAmount > 0) {
                        user.addCommissionPaid(eventId, optionName, commissionAmount);
                        event.addCommission(commissionAmount);
                        if (user.getBalance() < 0) user.setBlocked(true);
                    }

                    // 2. עדכון המוכר (המשתמש ששם את הפקודה המקורית)
                    engine.models.User seller = currentUsers.get(existingSell.getUsername());
                    if (seller != null) {
                        seller.addFunds(matchAmount);
                        seller.addHolding(eventId, optionName, -matchQty);
                        seller.addInvestment(eventId, optionName, -matchAmount);
                    }

                    // 3. הוספת תיעוד לעסקאות ועדכון נפח מניות באופציה
                    event.addTradeRecord(new engine.models.TradeRecord(username, optionName, matchQty, existingSell.getPrice(), commissionAmount));
                    event.getOptions().stream().filter(o -> o.getName().equals(optionName)).findFirst().ifPresent(o -> o.addShares(matchQty));

                    if (existingSell.isCompleted()) {
                        sellIterator.remove();
                    }
                }
            }

            // --- MINT: demand for both opposite options together covering the base value (d)
            // mints a brand-new pair of shares, funded jointly by both buyers ---
            if (remainingQtyToProcess > 0 && event.isAllowMint()) {
                String otherOptionName = event.getOptions().stream()
                        .map(engine.models.Option::getName)
                        .filter(n -> !n.equals(optionName))
                        .findFirst().orElse(null);

                if (otherOptionName != null) {
                    // Snapshot of resting opposite-option buy orders, best price first (buyOrders is
                    // globally price-sorted, so filtering preserves that relative order)
                    java.util.List<engine.models.Order> oppositeBuys = event.getBuyOrders().stream()
                            .filter(o -> o.getOptionName().equals(otherOptionName))
                            .collect(java.util.stream.Collectors.toList());

                    for (engine.models.Order oppositeBuy : oppositeBuys) {
                        if (remainingQtyToProcess <= 0) break;

                        double restingBuyerPrice = oppositeBuy.getPrice();
                        if (price + restingBuyerPrice < event.getDValue()) {
                            break; // best remaining opposite order doesn't qualify; none lower will either
                        }

                        int mintQty = Math.min(remainingQtyToProcess, oppositeBuy.getRemainingQuantity());
                        double incomingBuyerPrice = event.getDValue() - restingBuyerPrice;

                        double incomingCommission = event.getCommissionType() == engine.models.CommissionType.ON_PURCHASE
                                ? (incomingBuyerPrice * mintQty) * (event.getCommission() / 100.0) : 0.0;
                        double restingCommission = event.getCommissionType() == engine.models.CommissionType.ON_PURCHASE
                                ? (restingBuyerPrice * mintQty) * (event.getCommission() / 100.0) : 0.0;

                        // Incoming buyer pays the complementary price now (funds not frozen yet for this order)
                        user.deductFunds(incomingBuyerPrice * mintQty + incomingCommission);
                        user.addHolding(eventId, optionName, mintQty);
                        user.addInvestment(eventId, optionName, incomingBuyerPrice * mintQty);
                        if (incomingCommission > 0) {
                            user.addCommissionPaid(eventId, optionName, incomingCommission);
                            event.addCommission(incomingCommission);
                            if (user.getBalance() < 0) user.setBlocked(true);
                        }

                        // Resting buyer already had funds frozen at their own price when they placed the order
                        engine.models.User restingBuyer = currentUsers.get(oppositeBuy.getUsername());
                        if (restingBuyer != null) {
                            restingBuyer.addHolding(eventId, otherOptionName, mintQty);
                            restingBuyer.addInvestment(eventId, otherOptionName, restingBuyerPrice * mintQty);
                            if (restingCommission > 0) {
                                restingBuyer.deductFunds(restingCommission);
                                restingBuyer.addCommissionPaid(eventId, otherOptionName, restingCommission);
                                event.addCommission(restingCommission);
                                if (restingBuyer.getBalance() < 0) restingBuyer.setBlocked(true);
                            }
                        }

                        event.addFundsToAccount(event.getDValue() * mintQty);

                        event.addTradeRecord(new engine.models.TradeRecord(username, optionName, mintQty, incomingBuyerPrice, incomingCommission));
                        event.addTradeRecord(new engine.models.TradeRecord(oppositeBuy.getUsername(), otherOptionName, mintQty, restingBuyerPrice, restingCommission));
                        event.getOptions().stream().filter(o -> o.getName().equals(optionName)).findFirst().ifPresent(o -> o.addShares(mintQty));
                        event.getOptions().stream().filter(o -> o.getName().equals(otherOptionName)).findFirst().ifPresent(o -> o.addShares(mintQty));

                        oppositeBuy.reduceQuantity(mintQty);
                        remainingQtyToProcess -= mintQty;
                    }

                    event.getBuyOrders().removeIf(engine.models.Order::isCompleted);
                }
            }

            if (remainingQtyToProcess > 0) {
                user.deductFunds(price * remainingQtyToProcess); // הקפאת כספים לפקודה פתוחה
                event.addBuyOrder(new engine.models.Order(username, eventId, optionName, orderType, price, remainingQtyToProcess));
            }

        } else { // SELL
            // --- חסימת מכירה ללא כיסוי (Short Selling) וניהול Market Maker ---
            int userCurrentHoldings = 0;
            if (user.getHoldings() != null && user.getHoldings().containsKey(eventId)) {
                userCurrentHoldings = user.getHoldings().get(eventId).getOrDefault(optionName, 0);
            }

            boolean isMarketMaker = username.equals(event.getMarketMaker());

            // אם המשתמש הוא לא עושה השוק, או שהוא עושה השוק אבל אין אישור הדפסה
            if (!isMarketMaker || !event.isAllowMint()) {
                if (userCurrentHoldings < quantity) {
                    throw new engine.exception.GuessMarketException(
                            String.format("Error: You only own %d shares of '%s'. Cannot sell %d shares.",
                                    userCurrentHoldings, optionName, quantity)
                    );
                }
            }
            // ----------------------------------------------------------------------

            java.util.Iterator<engine.models.Order> buyIterator = event.getBuyOrders().iterator();
            while (buyIterator.hasNext() && remainingQtyToProcess > 0) {
                engine.models.Order existingBuy = buyIterator.next();

                if (existingBuy.getOptionName().equals(optionName) && existingBuy.getPrice() >= price) {
                    int matchQty = Math.min(remainingQtyToProcess, existingBuy.getRemainingQuantity());

                    existingBuy.reduceQuantity(matchQty);
                    remainingQtyToProcess -= matchQty;

                    // Commission is charged only to the buyer, at the moment their purchase completes
                    double matchAmount = existingBuy.getPrice() * matchQty;
                    double commissionAmount = event.getCommissionType() == engine.models.CommissionType.ON_PURCHASE
                            ? matchAmount * (event.getCommission() / 100.0) : 0.0;

                    // 1. עדכון המוכר (המשתמש הנוכחי)
                    user.addFunds(matchAmount);
                    user.addHolding(eventId, optionName, -matchQty); // יורד למינוס רק אם זה MM שמדפיס
                    user.addInvestment(eventId, optionName, -matchAmount);

                    // 2. עדכון הקונה (המשתמש ששם את הפקודה המקורית)
                    engine.models.User buyer = currentUsers.get(existingBuy.getUsername());
                    if (buyer != null) {
                        buyer.addHolding(eventId, optionName, matchQty);
                        // הכסף כבר הוקפא ממנו כשהוא הניח את פקודת הקנייה; העמלה נגבית רק עכשיו, במימוש הקנייה בפועל
                        buyer.addInvestment(eventId, optionName, matchAmount);
                        if (commissionAmount > 0) {
                            buyer.deductFunds(commissionAmount);
                            buyer.addCommissionPaid(eventId, optionName, commissionAmount);
                            event.addCommission(commissionAmount);
                            if (buyer.getBalance() < 0) buyer.setBlocked(true);
                        }
                    }

                    // 3. הוספת תיעוד לעסקאות ועדכון נפח מניות באופציה
                    event.addTradeRecord(new engine.models.TradeRecord(username, optionName, matchQty, existingBuy.getPrice(), commissionAmount));
                    event.getOptions().stream().filter(o -> o.getName().equals(optionName)).findFirst().ifPresent(o -> o.addShares(matchQty));

                    if (existingBuy.isCompleted()) {
                        buyIterator.remove();
                    }
                }
            }

            if (remainingQtyToProcess > 0) {
                event.addSellOrder(new engine.models.Order(username, eventId, optionName, orderType, price, remainingQtyToProcess));
            }
        }
        if (user.getBalance() < 0) {
            user.setBlocked(true);
        }
    }

    public void openEvent(int eventId, String username) {
        engine.models.User user = currentUsers.get(username);
        if (user == null) {
            throw new engine.exception.GuessMarketException("Error: User '" + username + "' not found.");
        }
        if (user.isBlocked()) {
            throw new engine.exception.GuessMarketException("Error: User '" + username + "' is blocked due to a negative balance.");
        }

        Event event = currentEvents.get(eventId);
        if (event == null) throw new engine.exception.GuessMarketException("Error: Event with ID " + eventId + " not found.");

        if (event.getStatus() == engine.models.EventStatus.ACTIVE || event.isClosed()) {
            throw new engine.exception.GuessMarketException("Error: Event is already open or closed.");
        }

        if (!username.equals(event.getMarketMaker())) {
            throw new engine.exception.GuessMarketException("Error: Only the Market Maker (" + event.getMarketMaker() + ") can open this event.");
        }

        if (event.isLMSR()) {
            // חישוב סובסידיה: b * ln(מספר האופציות)
            double subsidy = event.getB() * Math.log(event.getOptions().size());
            // Spec: the MM cannot open an event without enough funds to cover it - unlike
            // regular trades, this is a hard gate, not an allow-then-block case.
            if (user.getBalance() < subsidy) {
                throw new engine.exception.GuessMarketException(String.format("Error: Insufficient funds. Market Maker needs $%.2f to open the LMSR event.", subsidy));
            }
            user.deductFunds(subsidy);
            // הקרן של האירוע כבר אותחלה בבנאי של Event עם b*ln(2), אז אין צורך להוסיף פעמיים
        } else {
            // Order Book: תשלום של (initial * d) וקבלת כמות המניות הראשונית
            int initialShares = (int) event.getInitialPrice();
            double cost = initialShares * event.getDValue();
            if (user.getBalance() < cost) {
                throw new engine.exception.GuessMarketException(String.format("Error: Insufficient funds. Market Maker needs $%.2f to mint the initial shares.", cost));
            }

            user.deductFunds(cost);
            event.addFundsToAccount(cost);

            // חלוקת המניות הראשוניות לעושה השוק
            // cost buys one pair of shares (one of each option) per unit, so it is split evenly across options
            double costPerOption = cost / event.getOptions().size();
            for (engine.models.Option opt : event.getOptions()) {
                user.addHolding(eventId, opt.getName(), initialShares);
                user.addInvestment(eventId, opt.getName(), costPerOption);
                opt.addShares(initialShares);
            }
        }

        event.setStatus(engine.models.EventStatus.ACTIVE);
        if (user.getBalance() < 0) {
            user.setBlocked(true);
        }
    }

    @Override
    public int createLmsrEvent(String creatorUsername, String name, String description,
                                int commission, engine.models.CommissionType commissionType,
                                String option1, String option2, int b) {
        engine.models.User creator = validateEventCreation(creatorUsername, name, commission, option1, option2);
        if (b <= 0) {
            throw new engine.exception.GuessMarketException("Error: LMSR liquidity 'b' must be greater than 0.");
        }

        int newId = nextEventId();
        Event newEvent = new Event(newId, name, description, commission, commissionType, b, true);
        newEvent.addOption(new engine.models.Option(option1));
        newEvent.addOption(new engine.models.Option(option2));
        newEvent.setOrderBookParams(creatorUsername, false, 0.0, 0);

        currentEvents.put(newId, newEvent);
        return newId;
    }

    @Override
    public int createOrderBookEvent(String creatorUsername, String name, String description,
                                     int commission, engine.models.CommissionType commissionType,
                                     String option1, String option2, int initial, int d, boolean allowMint) {
        engine.models.User creator = validateEventCreation(creatorUsername, name, commission, option1, option2);
        if (initial <= 0) {
            throw new engine.exception.GuessMarketException("Error: Initial share count must be greater than 0.");
        }
        if (d <= 0) {
            throw new engine.exception.GuessMarketException("Error: Base value 'd' must be greater than 0.");
        }

        int newId = nextEventId();
        Event newEvent = new Event(newId, name, description, commission, commissionType, 0, false);
        newEvent.addOption(new engine.models.Option(option1));
        newEvent.addOption(new engine.models.Option(option2));
        newEvent.setOrderBookParams(creatorUsername, allowMint, initial, d);

        currentEvents.put(newId, newEvent);
        return newId;
    }

    private engine.models.User validateEventCreation(String creatorUsername, String name, int commission,
                                                       String option1, String option2) {
        engine.models.User creator = currentUsers.get(creatorUsername);
        if (creator == null) {
            throw new engine.exception.GuessMarketException("Error: User '" + creatorUsername + "' not found.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new engine.exception.GuessMarketException("Error: Event name cannot be empty.");
        }
        if (commission < 0 || commission > 90) {
            throw new engine.exception.InvalidCommissionException(name, commission);
        }
        if (option1 == null || option1.trim().isEmpty() || option2 == null || option2.trim().isEmpty()) {
            throw new engine.exception.GuessMarketException("Error: Both option names must be provided.");
        }
        if (option1.trim().equalsIgnoreCase(option2.trim())) {
            throw new engine.exception.GuessMarketException("Error: The two options must have different names.");
        }
        return creator;
    }

    private int nextEventId() {
        return currentEvents.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

}