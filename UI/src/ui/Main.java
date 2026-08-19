package ui;

import engine.core.GuessMarketEngine;
import engine.core.IGuessMarketEngine;
import engine.dto.EventDTO;
import engine.dto.OptionDTO;
import engine.dto.TradeDTO;
import engine.dto.TradeReceiptDTO;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        IGuessMarketEngine engine = new GuessMarketEngine();

        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("Welcome to Guess Market!");

        while (isRunning) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Load system details file");
            System.out.println("2. Show events");
            System.out.println("3. Show event trade status");
            System.out.println("4. Participate in an event");
            System.out.println("5. Close an event");
            System.out.println("6. Save system state to file");
            System.out.println("7. Load system state from file");
            System.out.println("8. Exit");
            System.out.print("Please enter your choice (1-8): ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleLoadFile(scanner, engine);
                    break;
                case "2":
                    showEvents(engine);
                    break;
                case "3":
                    handleShowEventStatus(scanner, engine);
                    break;
                case "4":
                    handleBuyShares(scanner, engine);
                    break;
                case "5":
                    handleCloseEvent(scanner, engine);
                    break;
                case "6":
                    handleSaveState(scanner, engine);
                    break;
                case "7":
                    handleLoadState(scanner, engine);
                    break;
                case "8":
                    System.out.println("Exiting Guess Market. Goodbye!");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // Option 1: Load all events from a XML file
    private static void handleLoadFile(Scanner scanner, IGuessMarketEngine engine) {
        if (!engine.getAllEvents().isEmpty()) {
            System.out.println("Warning: Loading a new file will overwrite the current system state.");
        }
        System.out.print("Please enter the full XML file path: ");
        String filePath = scanner.nextLine().trim();

        filePath = filePath.replace("\"", "");

        try {
            engine.loadEventsFromFile(filePath);
            System.out.println("File loaded successfully! The system has been updated.");
        } catch (engine.exception.GuessMarketException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Option 2: Show all events in the system
    private static void showEvents(IGuessMarketEngine engine) {
        if (isSystemEmpty(engine)) return;
        Map<Integer, EventDTO> events = engine.getAllEvents();

        for (EventDTO event : events.values()) {
            printEventBasicDetails(event);
        }
        System.out.println("\n------------------------------------------------");
    }

    // Option 3: Show all event information by ID
    private static void handleShowEventStatus(Scanner scanner, IGuessMarketEngine engine) {
        if (isSystemEmpty(engine)) return;
        showEvents(engine);
        System.out.print("Please enter Event ID: ");

        try {
            int eventId = Integer.parseInt(scanner.nextLine().trim());
            EventDTO eventDTO = engine.getEventById(eventId);

            if (eventDTO == null) {
                System.out.println("Error: Event not found.");
                return;
            }
            printEventStatus(eventDTO);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter valid numbers.");
        }
    }

    // Option 4: handle user's buy shares process
    private static void handleBuyShares(Scanner scanner, IGuessMarketEngine engine) {
        if (isSystemEmpty(engine)) return;

        if (!showActiveEvents(engine)) return;

        System.out.print("\nEnter Event ID to participate: ");
        try {
            int eventId = Integer.parseInt(scanner.nextLine().trim());
            EventDTO eventDTO = engine.getEventById(eventId);
            if (eventDTO == null || !eventDTO.getStatus().equals("ACTIVE")) {
                System.out.println("Error: Event not found or not active."); return;
            }

            printEventStatus(eventDTO);

            System.out.print("\nSelect option (1-" + eventDTO.getOptions().size() + "): ");
            int optionChoice = Integer.parseInt(scanner.nextLine().trim());
            if (optionChoice < 1 || optionChoice > eventDTO.getOptions().size()) {
                System.out.println("Error: Invalid option selected.");
                return;
            }

            System.out.print("Enter amount of shares to buy: ");
            int sharesToBuy = Integer.parseInt(scanner.nextLine().trim());

            TradeReceiptDTO receipt = engine.buyShares(eventId, optionChoice - 1, sharesToBuy);
            System.out.println("\n--- Trade Successful ---");
            System.out.println("Cost of shares: $" + String.format("%.2f", receipt.sharesCost));
            if (receipt.commission > 0) {
                System.out.println("Commission paid: $" + String.format("%.2f", receipt.commission));
            }

            System.out.println("Total paid: $" + String.format("%.2f", receipt.totalPaid));

            printEventStatus(engine.getEventById(eventId));
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter valid numbers only.");
        } catch (engine.exception.GuessMarketException e) {
            System.out.println(e.getMessage());
        }
    }

    // Option 5: handle close event by ID
    private static void handleCloseEvent(Scanner scanner, IGuessMarketEngine engine) {
        if (isSystemEmpty(engine)) return;

        if (!showActiveEvents(engine)) return;

        System.out.print("\nPlease enter Event ID to close: ");
        try {
            int eventIdToClose = Integer.parseInt(scanner.nextLine().trim());
            EventDTO eventToClose = engine.getEventById(eventIdToClose);

            if (eventToClose == null || !eventToClose.getStatus().equals("ACTIVE")) {
                System.out.println("Error: Event not found or already closed.");
                return;
            }

            printEventStatus(eventToClose);

            System.out.println("\nSelect the winning option:");
            for (int i = 0; i < eventToClose.getOptions().size(); i++) {
                System.out.println((i + 1) + ". " + eventToClose.getOptions().get(i).getName());
            }

            System.out.print("Enter choice (1-" + eventToClose.getOptions().size() + "): ");
            int winChoice = Integer.parseInt(scanner.nextLine().trim());

            if (winChoice < 1 || winChoice > eventToClose.getOptions().size()) {
                System.out.println("Error: Invalid winning option selected.");
                return;
            }

            engine.closeEvent(eventIdToClose, winChoice - 1);
            System.out.println("\nEvent closed successfully!");

            System.out.println("\n--- Event Final Summary ---");
            printEventStatus(engine.getEventById(eventIdToClose));

        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter valid numbers only.");
        } catch (engine.exception.GuessMarketException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Option 6: save current system state using serialization
    private static void handleSaveState(Scanner scanner, IGuessMarketEngine engine) {
        System.out.print("Enter full path to save the system state (e.g., C:/temp/market.dat): ");
        String savePath = scanner.nextLine().trim().replace("\"", "");
        try {
            engine.saveSystemState(savePath);
            System.out.println("System state saved successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Option 7: load system state using serialization
    private static void handleLoadState(Scanner scanner, IGuessMarketEngine engine) {
        System.out.print("Enter full path to load the system state (e.g., C:/temp/market.dat): ");
        String loadPath = scanner.nextLine().trim().replace("\"", "");
        try {
            engine.loadSystemState(loadPath);
            System.out.println("System state loaded successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean isSystemEmpty(IGuessMarketEngine engine) {
        if (engine.getAllEvents().isEmpty()) {
            System.out.println("Error: No valid file is currently loaded in the system. Please load a file first (Option 1).");
            return true;
        }
        return false;
    }

    private static void printEventStatus(EventDTO eventDTO) {
        System.out.println("\n--- Event Status: " + eventDTO.getName() + " ---");
        System.out.println("Options Current State:");
        int optIndex = 1;
        for (OptionDTO opt : eventDTO.getOptions()) {
            System.out.println(optIndex + ". " + opt.getName() +
                    " | Price/Probability: " + String.format("%.2f", opt.getCurrentPrice()) +
                    " | Shares Bought: " + opt.getSharesBought());
            optIndex++;
        }
        System.out.println("\nEvent Account Balance: $" + String.format("%.2f", eventDTO.getAccountBalance()));
        System.out.println("Total Commission Collected: $" + String.format("%.2f", eventDTO.getTotalCommissionCollected()));

        System.out.println("\nTrade History (Newest to Oldest):");
        if (eventDTO.getTradeHistory().isEmpty()) {
            System.out.println("No trades yet.");
        } else {
            for (TradeDTO trade : eventDTO.getTradeHistory()) {
                System.out.println("Bought " + trade.getSharesQuantity() + " shares of '" + trade.getOptionName() +
                        "' for $" + String.format("%.2f", trade.getPricePaid()));
            }
        }
        if (eventDTO.getWinningOption() != null) {
            System.out.println("\n*** EVENT CLOSED. Winner: " + eventDTO.getWinningOption() + " ***");
        }
    }

    private static void printEventBasicDetails(EventDTO event) {
        System.out.println("\n------------------------------------------------");
        System.out.println("Event ID: " + event.getId());
        System.out.println("Name: " + event.getName());
        System.out.println("Description: " + event.getDescription());
        System.out.println("Commission: " + event.getCommission() + "%");
        System.out.println("Commission Type: " + event.getCommissionType());
        System.out.print("Options: ");
        for (int i = 0; i < event.getOptions().size(); i++) {
            OptionDTO opt = event.getOptions().get(i);
            System.out.print(opt.getName());
            if (i < event.getOptions().size() - 1) {
                System.out.print(" / ");
            }
        }
        System.out.print("\nStatus: " + event.getStatus());
    }

    private static boolean showActiveEvents(IGuessMarketEngine engine) {
        System.out.println("\n--- Active Events ---");
        boolean hasActive = false;
        for (EventDTO event : engine.getAllEvents().values()) {
            if ("ACTIVE".equals(event.getStatus())) {
                printEventBasicDetails(event);
                hasActive = true;
            }
        }

        if (hasActive) {
            System.out.println("\n------------------------------------------------");
        } else {
            System.out.println("No active events available.");
        }

        return hasActive;
    }
}