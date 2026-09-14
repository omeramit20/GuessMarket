package ui;

import engine.dto.EventDTO;
import engine.models.CommissionType;
import engine.models.Order;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import javafx.collections.transformation.*;

public class MainController {

    private final engine.core.IGuessMarketEngine engine = new engine.core.GuessMarketEngine();

    @FXML private javafx.scene.layout.BorderPane rootPane;
    @FXML private ComboBox<String> cbTheme;
    @FXML private CheckBox chkAnimations;
    @FXML private Button btnCreateEvent;
    @FXML private Label lblEventsSummary;

    @FXML private VBox eventDetailsRoot;
    @FXML private VBox userDetailsRoot;
    @FXML private VBox tradeFormCard;
    @FXML private ScrollPane eventDetailsScroll;
    @FXML private ScrollPane userDetailsScroll;
    @FXML private javafx.scene.chart.LineChart<Number, Number> priceChart;
    @FXML private javafx.scene.chart.LineChart<Number, Number> balanceChart;
    @FXML private Label lblPriceChartTitle;
    @FXML private Label lblBalanceChartTitle;
    @FXML private Label lblStateSubtitle;
    @FXML private Label lblOpenHint;
    @FXML private Label lblCloseHint;

    @FXML private TextField filePathField;
    @FXML private Button loadFileBtn;
    @FXML private ProgressBar progressBar;

    @FXML private ToggleButton chkTypeLMSR;
    @FXML private ToggleButton chkTypeOB;
    @FXML private ToggleButton chkStatusNotActive;
    @FXML private ToggleButton chkStatusActive;
    @FXML private ToggleButton chkStatusClosed;
    @FXML private ToggleButton chkCommPurchase;
    @FXML private ToggleButton chkCommClose;

    // הרשימה המסוננת שתקושר לטבלה
    private FilteredList<engine.dto.EventDTO> filteredEvents;

    @FXML private TableView<engine.dto.EventDTO> eventsTable;
    @FXML private TableColumn<engine.dto.EventDTO, String> colEventName;
    @FXML private TableColumn<engine.dto.EventDTO, String> colEventStatus;
    @FXML private TableColumn<engine.dto.EventDTO, String> colEventType;
    @FXML private TableColumn<engine.dto.EventDTO, String> colEventComm;
    @FXML private TableColumn<engine.dto.EventDTO, Double> colEventBalance;
    @FXML private TableColumn<engine.dto.EventDTO, String> colEventCommMethod;

    @FXML private Label lblSelectedEventName;
    @FXML private Label lblOption1Data;
    @FXML private Label lblOption2Data;

    @FXML private Label lblOpt1Name;
    @FXML private Label lblOpt2Name;

    @FXML private TableView<engine.dto.TradeDTO> tradeHistoryTable;
    @FXML private TableColumn<engine.dto.TradeDTO, String> colTradeOption;
    @FXML private TableColumn<engine.dto.TradeDTO, Integer> colTradeQuantity;
    @FXML private TableColumn<engine.dto.TradeDTO, Double> colTradePrice;

    @FXML private ListView<String> usersListView;
    @FXML private Label lblUserName;
    @FXML private Label lblUserBalance;
    @FXML private TableView<engine.dto.UserHoldingDTO> portfolioTable;

    @FXML private ComboBox<engine.dto.UserEventParticipationDTO> cbUserEvent;
    @FXML private Label lblParticipationWinner;
    @FXML private Label lblParticipationCommission;
    @FXML private Label lblParticipationPL;

    @FXML private TableView<engine.dto.TradeDTO> userTradeHistoryTable;
    @FXML private TableColumn<engine.dto.TradeDTO, String> colUserTradeOption;
    @FXML private TableColumn<engine.dto.TradeDTO, Integer> colUserTradeQty;
    @FXML private TableColumn<engine.dto.TradeDTO, Double> colUserTradePrice;
    @FXML private TableColumn<engine.dto.TradeDTO, Double> colUserTradeCommission;

    @FXML private TableView<engine.dto.OptionParticipationDTO> userOptionBreakdownTable;
    @FXML private TableColumn<engine.dto.OptionParticipationDTO, String> colBreakdownOption;
    @FXML private TableColumn<engine.dto.OptionParticipationDTO, Integer> colBreakdownQty;
    @FXML private TableColumn<engine.dto.OptionParticipationDTO, Double> colBreakdownPaid;
    @FXML private TableColumn<engine.dto.OptionParticipationDTO, Double> colBreakdownCommission;

    @FXML private ComboBox<String> cbActiveUser;
    @FXML private ComboBox<String> cbBuyOption;
    @FXML private TextField txtSharesAmount;
    @FXML private Button btnBuy;
    @FXML private Button btnOpenEvent;

    @FXML private ComboBox<engine.models.Order.OrderType> cbOrderType;
    @FXML private TextField txtOrderPrice;
    @FXML private Label lblStateTitle;

    @FXML private ScrollPane tilesScrollPane;
    @FXML private javafx.scene.layout.FlowPane eventsTilePane;

    @FXML private javafx.scene.control.ToggleGroup viewToggleGroup;
    @FXML private javafx.scene.control.ToggleButton btnTableView;
    @FXML private javafx.scene.control.ToggleButton btnTilesView;

    @FXML private Label lblEventBalance;
    @FXML private Label lblCommissionCollected;
    @FXML private Label lblWinner;
    @FXML private VBox orderBookContainer;

    // Order Book Tables
    @FXML private TableView<engine.dto.OrderDTO> buyOrdersTable;
    @FXML private TableColumn<engine.dto.OrderDTO, String> colBuyUser;
    @FXML private TableColumn<engine.dto.OrderDTO, String> colBuyOption;
    @FXML private TableColumn<engine.dto.OrderDTO, Integer> colBuyQty;
    @FXML private TableColumn<engine.dto.OrderDTO, Double> colBuyPrice;

    @FXML private TableView<engine.dto.OrderDTO> sellOrdersTable;
    @FXML private TableColumn<engine.dto.OrderDTO, String> colSellUser;
    @FXML private TableColumn<engine.dto.OrderDTO, String> colSellOption;
    @FXML private TableColumn<engine.dto.OrderDTO, Integer> colSellQty;
    @FXML private TableColumn<engine.dto.OrderDTO, Double> colSellPrice;

    // Participants Table
    @FXML private TableView<engine.dto.UserHoldingDTO> eventParticipantsTable;
    @FXML private TableColumn<engine.dto.UserHoldingDTO, String> colPartUser;
    @FXML private TableColumn<engine.dto.UserHoldingDTO, String> colPartOption;
    @FXML private TableColumn<engine.dto.UserHoldingDTO, Integer> colPartQty;

    // Close Event
    @FXML private javafx.scene.layout.FlowPane closeEventBox;
    @FXML private ComboBox<String> cbWinningOption;
    @FXML private Button btnCloseEvent;

    // Portfolio Column fixes
    @FXML private TableColumn<engine.dto.UserHoldingDTO, String> colPortEvent;
    @FXML private TableColumn<engine.dto.UserHoldingDTO, String> colPortOption;
    @FXML private TableColumn<engine.dto.UserHoldingDTO, Integer> colPortQty;

    @FXML
    public void initialize() {
        eventParticipantsTable.setPlaceholder(new Label("No one has traded this event yet"));
        tradeHistoryTable.setPlaceholder(new Label("No trades yet"));
        buyOrdersTable.setPlaceholder(new Label("No pending buy orders"));
        sellOrdersTable.setPlaceholder(new Label("No pending sell orders"));
        portfolioTable.setPlaceholder(new Label("No holdings yet"));
        userTradeHistoryTable.setPlaceholder(new Label("No trades yet"));
        userOptionBreakdownTable.setPlaceholder(new Label("No holdings yet"));

        colEventName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colEventStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        colEventType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));

        colEventComm.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCommission() + "%"));
        colEventBalance.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAccountBalance()));

        colEventBalance.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (empty || balance == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", balance));
                }
            }
        });
        colEventCommMethod.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCommissionType()));

        colTradeOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colTradeQuantity.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSharesQuantity()));
        colTradePrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPricePaid()));
        colTradePrice.setCellFactory(col -> currencyCell());

        eventsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateEventDetails(newValue);
            }
        });

        usersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUserDetails(newValue);
            }
        });

        // Portfolio table column bindings
        // Portfolio fixes
        colPortEvent.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEventName()));
        colPortOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colPortQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        // Order Book bindings
        colBuyUser.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        colBuyOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colBuyQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRemainingQuantity()));
        colBuyPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colBuyPrice.setCellFactory(col -> currencyCell());

        colSellUser.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        colSellOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colSellQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRemainingQuantity()));
        colSellPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colSellPrice.setCellFactory(col -> currencyCell());

        // Participants bindings
        colPartUser.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        colPartOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colPartQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        // האזנה לשינוי משתמש כדי לעדכן הרשאות בזמן אמת (סעיף 3)
        cbActiveUser.valueProperty().addListener((obs, oldVal, newVal) -> {
            engine.dto.EventDTO selected = eventsTable.getSelectionModel().getSelectedItem();
            if (selected != null) updateEventDetails(selected);
        });

        cbOrderType.getItems().setAll(Order.OrderType.values());
        cbOrderType.getSelectionModel().select(Order.OrderType.BUY);

        viewToggleGroup = new ToggleGroup();
        btnTableView.setToggleGroup(viewToggleGroup);
        btnTilesView.setToggleGroup(viewToggleGroup);

        // User event participation (Users tab)
        colUserTradeOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colUserTradeQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSharesQuantity()));
        colUserTradePrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPricePaid()));
        colUserTradePrice.setCellFactory(col -> currencyCell());
        colUserTradeCommission.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCommissionPaid()));
        colUserTradeCommission.setCellFactory(col -> currencyCell());

        colBreakdownOption.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionName()));
        colBreakdownQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colBreakdownPaid.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmountPaid()));
        colBreakdownPaid.setCellFactory(col -> currencyCell());
        colBreakdownCommission.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCommissionPaid()));
        colBreakdownCommission.setCellFactory(col -> currencyCell());

        cbUserEvent.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(engine.dto.UserEventParticipationDTO dto) {
                if (dto == null) return "";
                return dto.getEventName() + " (" + dto.getEventType() + ", " + dto.getEventStatus() + ")";
            }

            @Override
            public engine.dto.UserEventParticipationDTO fromString(String string) {
                return null;
            }
        });
        cbUserEvent.valueProperty().addListener((obs, oldVal, newVal) -> updateParticipationDetails(newVal));

        cbTheme.getItems().setAll("Classic", "Dark", "Forest");
        cbTheme.getSelectionModel().select("Classic");
        cbTheme.valueProperty().addListener((obs, oldVal, newVal) -> applyTheme(newVal));

        chkAnimations.setSelected(true);
    }

    private void applyTheme(String theme) {
        String cssFile = switch (theme) {
            case "Dark" -> "/style-dark.css";
            case "Forest" -> "/style-forest.css";
            default -> "/style.css";
        };
        rootPane.getStylesheets().setAll(getClass().getResource(cssFile).toExternalForm());
    }

    // Bonus: short animations accompanying app flow, disable-able via chkAnimations (spec cap: <=2s each)
    private void playFadeIn(javafx.scene.Node node) {
        if (chkAnimations == null || !chkAnimations.isSelected()) return;
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), node);
        ft.setFromValue(0.35);
        ft.setToValue(1.0);
        ft.play();
    }

    private void playPulse(javafx.scene.Node node) {
        if (chkAnimations == null || !chkAnimations.isSelected()) return;
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(160), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.12);
        st.setToY(1.12);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private <S> TableCell<S, Double> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
            }
        };
    }

    @FXML
    public void handleLoadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Guess Market XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        Stage stage = (Stage) loadFileBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            progressBar.setVisible(true);

            Task<Void> loadTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = 1; i <= 10; i++) {
                        Thread.sleep(150);
                        updateProgress(i, 10);
                    }

                    engine.loadEventsFromFile(selectedFile.getAbsolutePath());
                    return null;
                }
            };

            loadTask.setOnSucceeded(e -> {
                progressBar.setVisible(false);

                // Update events table with FilteredList
                java.util.Map<Integer, engine.dto.EventDTO> events = engine.getAllEvents();
                javafx.collections.ObservableList<engine.dto.EventDTO> eventList =
                        javafx.collections.FXCollections.observableArrayList(events.values());

                filteredEvents = new javafx.collections.transformation.FilteredList<>(eventList, p -> true);
                eventsTable.setItems(filteredEvents);
                updateEventsSummary();

                // Update users list view and active user combo box
                java.util.Map<String, engine.dto.UserDTO> users = engine.getAllUsers();
                usersListView.getItems().clear();
                usersListView.getItems().addAll(users.keySet());

                cbActiveUser.getItems().clear();
                cbActiveUser.getItems().addAll(users.keySet());

                System.out.println("Data loaded and UI updated!");
            });

            loadTask.setOnFailed(e -> {
                Throwable error = loadTask.getException();
                System.out.println("Error loading file: " + error.getMessage());
                progressBar.setVisible(false);
                showAlert(Alert.AlertType.ERROR, "Load Failed", error.getMessage());
            });

            progressBar.progressProperty().bind(loadTask.progressProperty());
            new Thread(loadTask).start();
        }
    }

    private void updateEventDetails(EventDTO event) {
        if (eventDetailsScroll != null) {
            eventDetailsScroll.setVvalue(0);
        }
        lblSelectedEventName.setText(event.getName());

        if (event.getOptions() != null && event.getOptions().size() >= 2) {
            engine.dto.OptionDTO opt1 = event.getOptions().get(0);
            engine.dto.OptionDTO opt2 = event.getOptions().get(1);

            lblOption1Data.setWrapText(true);
            lblOption2Data.setWrapText(true);
            lblOption1Data.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            lblOption2Data.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

            lblOpt1Name.setText(opt1.getName());
            lblOpt2Name.setText(opt2.getName());

            if ("OrderBook".equals(event.getType())) {
                lblStateTitle.setText("Current State (Order Book)");
                lblStateSubtitle.setText("Peer-to-peer trading: your order fills against other users' buy/sell orders.");
                lblOption1Data.setText(String.format("LAST: $%.2f\nBID: $%.2f | ASK: $%.2f\nMID: $%.2f | SPREAD: $%.2f",
                        opt1.getLast(), opt1.getBid(), opt1.getAsk(), opt1.getMid(), opt1.getSpread()));
                lblOption2Data.setText(String.format("LAST: $%.2f\nBID: $%.2f | ASK: $%.2f\nMID: $%.2f | SPREAD: $%.2f",
                        opt2.getLast(), opt2.getBid(), opt2.getAsk(), opt2.getMid(), opt2.getSpread()));
            } else {
                lblStateTitle.setText("Current State (LMSR)");
                lblStateSubtitle.setText("Automated market maker: trade against the event's own pricing curve, no counterparty needed.");
                lblOption1Data.setText(String.format("Price: $%.2f\nShares: %d", opt1.getCurrentPrice(), opt1.getSharesBought()));
                lblOption2Data.setText(String.format("Price: $%.2f\nShares: %d", opt2.getCurrentPrice(), opt2.getSharesBought()));
            }
        }

        // אכלוס רשימת האופציות לקנייה
        cbBuyOption.getItems().clear();
        if (event.getOptions() != null) {
            for (engine.dto.OptionDTO opt : event.getOptions()) {
                cbBuyOption.getItems().add(opt.getName());
            }
        }

        // נעילה או פתיחה של שדה המחיר בהתאם לסוג האירוע
        if ("LMSR".equals(event.getType())) {
            txtOrderPrice.setDisable(true);
            txtOrderPrice.setPromptText("Auto");
            txtOrderPrice.clear();
        } else {
            txtOrderPrice.setDisable(false);
            txtOrderPrice.setPromptText("Price ($)");
        }

        // נניח ש-event הוא ה-EventDTO של האירוע שנבחר בטבלה
        boolean isNotActive = "NOT_ACTIVE".equals(event.getStatus());
        boolean isActive = "ACTIVE".equals(event.getStatus());

        lblEventBalance.setText(String.format("Balance: $%.2f", event.getAccountBalance()));
        lblEventBalance.getStyleClass().removeAll("stat-badge-positive", "stat-badge-danger");
        lblEventBalance.getStyleClass().add(event.getAccountBalance() >= 0 ? "stat-badge-positive" : "stat-badge-danger");
        lblCommissionCollected.setText(String.format("Commission Collected: $%.2f", event.getTotalCommissionCollected()));

        boolean isClosed = "CLOSED".equals(event.getStatus());
        lblWinner.setVisible(isClosed && event.getWinningOption() != null);
        lblWinner.setManaged(isClosed && event.getWinningOption() != null);
        if (isClosed && event.getWinningOption() != null) {
            lblWinner.setText("Winner: " + event.getWinningOption());
        }

        // עדכון Order Book
        boolean isOrderBook = "OrderBook".equals(event.getType());
        orderBookContainer.setVisible(isOrderBook);
        orderBookContainer.setManaged(isOrderBook);

        if (isOrderBook) {
            buyOrdersTable.setItems(javafx.collections.FXCollections.observableArrayList(event.getBuyOrders()));
            sellOrdersTable.setItems(javafx.collections.FXCollections.observableArrayList(event.getSellOrders()));
        }

        // עדכון משתתפים
        eventParticipantsTable.setItems(javafx.collections.FXCollections.observableArrayList(event.getParticipantsHoldings()));

        // אכלוס אפשרויות לסגירה
        cbWinningOption.getItems().clear();
        for (engine.dto.OptionDTO opt : event.getOptions()) {
            cbWinningOption.getItems().add(opt.getName());
        }

        // ניהול הרשאות MM - כל כפתור מוצג רק כשהוא רלוונטי למצב הנוכחי, לא רק disabled
        String activeUser = cbActiveUser.getValue();
        boolean isMM = activeUser != null && activeUser.equals(event.getMarketMaker());

        btnOpenEvent.setVisible(isNotActive);
        btnOpenEvent.setManaged(isNotActive);
        btnOpenEvent.setDisable(!isMM);

        boolean showOpenHint = isNotActive && !isMM;
        lblOpenHint.setVisible(showOpenHint);
        lblOpenHint.setManaged(showOpenHint);
        if (showOpenHint) {
            lblOpenHint.setText("Only the Market Maker (" + event.getMarketMaker() + ") can open this event. Select them as \"Acting as\" to open it.");
        }

        btnBuy.setVisible(isActive);
        btnBuy.setManaged(isActive);

        closeEventBox.setVisible(isActive && isMM);
        closeEventBox.setManaged(isActive && isMM);

        boolean showCloseHint = isActive && !isMM;
        lblCloseHint.setVisible(showCloseHint);
        lblCloseHint.setManaged(showCloseHint);
        if (showCloseHint) {
            lblCloseHint.setText("Only the Market Maker (" + event.getMarketMaker() + ") can close this event.");
        }

        // עדכון טבלת היסטוריית המסחר
        if (event.getTradeHistory() != null) {
            javafx.collections.ObservableList<engine.dto.TradeDTO> trades =
                    javafx.collections.FXCollections.observableArrayList(event.getTradeHistory());
            tradeHistoryTable.setItems(trades);
        } else {
            tradeHistoryTable.getItems().clear();
        }

        updatePriceChart(event);
        playFadeIn(eventDetailsRoot);
    }

    // Bonus: price-history chart - per-share execution price of each option, in chronological trade order
    private void updatePriceChart(EventDTO event) {
        priceChart.getData().clear();
        java.util.List<engine.dto.TradeDTO> history = event.getTradeHistory();
        boolean hasData = history != null && !history.isEmpty();

        lblPriceChartTitle.setVisible(hasData);
        lblPriceChartTitle.setManaged(hasData);
        priceChart.setVisible(hasData);
        priceChart.setManaged(hasData);
        if (!hasData) return;

        java.util.Map<String, javafx.scene.chart.XYChart.Series<Number, Number>> seriesByOption = new java.util.LinkedHashMap<>();
        int index = 0;
        // history is newest-first; walk backwards for chronological order
        for (int i = history.size() - 1; i >= 0; i--) {
            engine.dto.TradeDTO t = history.get(i);
            if (t.getSharesQuantity() <= 0) continue;
            double perSharePrice = t.getPricePaid() / t.getSharesQuantity();
            javafx.scene.chart.XYChart.Series<Number, Number> series = seriesByOption.computeIfAbsent(t.getOptionName(), name -> {
                javafx.scene.chart.XYChart.Series<Number, Number> s = new javafx.scene.chart.XYChart.Series<>();
                s.setName(name);
                return s;
            });
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(index, perSharePrice));
            index++;
        }
        priceChart.getData().addAll(seriesByOption.values());
    }

    private void updateUserDetails(String username) {
        if (userDetailsScroll != null) {
            userDetailsScroll.setVvalue(0);
        }
        java.util.Map<String, engine.dto.UserDTO> usersMap = engine.getAllUsers();
        engine.dto.UserDTO user = usersMap.get(username);

        if (user != null) {
            lblUserName.setText(user.getName());
            lblUserBalance.setText(String.format("Balance: $%.2f", user.getBalance()));
            lblUserBalance.getStyleClass().removeAll("stat-badge-positive", "stat-badge-danger");
            lblUserBalance.getStyleClass().add(user.getBalance() >= 0 ? "stat-badge-positive" : "stat-badge-danger");

            // Load user holdings into portfolio table
            java.util.List<engine.dto.UserHoldingDTO> holdings = engine.getUserHoldings(username);
            portfolioTable.setItems(javafx.collections.FXCollections.observableArrayList(holdings));

            java.util.List<engine.dto.UserEventParticipationDTO> participation = engine.getUserEventParticipation(username);
            cbUserEvent.setItems(javafx.collections.FXCollections.observableArrayList(participation));
            if (!participation.isEmpty()) {
                cbUserEvent.getSelectionModel().selectFirst();
            } else {
                cbUserEvent.getSelectionModel().clearSelection();
                updateParticipationDetails(null);
            }

            updateBalanceChart(username);
            playFadeIn(userDetailsRoot);
        }
    }

    // Bonus: account balance over time (each buy/sell/payout appends a snapshot to the user's ledger)
    private void updateBalanceChart(String username) {
        balanceChart.getData().clear();
        java.util.List<Double> history = engine.getUserBalanceHistory(username);
        // A single point (just the starting balance) isn't a "history" yet - nothing has happened.
        boolean hasData = history != null && history.size() > 1;

        lblBalanceChartTitle.setVisible(hasData);
        lblBalanceChartTitle.setManaged(hasData);
        balanceChart.setVisible(hasData);
        balanceChart.setManaged(hasData);
        if (!hasData) return;

        javafx.scene.chart.XYChart.Series<Number, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName(username);
        for (int i = 0; i < history.size(); i++) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(i, history.get(i)));
        }
        balanceChart.getData().add(series);
    }

    private void updateParticipationDetails(engine.dto.UserEventParticipationDTO dto) {
        boolean isLMSR = dto != null && "LMSR".equals(dto.getEventType());
        boolean isOrderBook = dto != null && "OrderBook".equals(dto.getEventType());
        boolean isClosed = dto != null && "CLOSED".equals(dto.getEventStatus());

        userTradeHistoryTable.setVisible(isLMSR);
        userTradeHistoryTable.setManaged(isLMSR);
        if (isLMSR) {
            userTradeHistoryTable.setItems(javafx.collections.FXCollections.observableArrayList(dto.getTradeHistory()));
        }

        userOptionBreakdownTable.setVisible(isOrderBook);
        userOptionBreakdownTable.setManaged(isOrderBook);
        if (isOrderBook) {
            userOptionBreakdownTable.setItems(javafx.collections.FXCollections.observableArrayList(dto.getOptionBreakdown()));
        }

        boolean showWinner = isLMSR && isClosed;
        lblParticipationWinner.setVisible(showWinner);
        lblParticipationWinner.setManaged(showWinner);

        lblParticipationCommission.setVisible(isOrderBook);
        lblParticipationCommission.setManaged(isOrderBook);
        if (isOrderBook) {
            lblParticipationCommission.setText(String.format("Total Commission Paid: $%.2f", dto.getTotalCommissionPaid()));
        }

        boolean showPL = isOrderBook && isClosed && dto.getProfitLoss() != null;
        lblParticipationPL.setVisible(showPL);
        lblParticipationPL.setManaged(showPL);
        if (showPL) {
            lblParticipationPL.setText(String.format("Profit/Loss: $%.2f", dto.getProfitLoss()));
        }

        if (showWinner) {
            engine.dto.EventDTO fullEvent = engine.getEventById(dto.getEventId());
            if (fullEvent != null && fullEvent.getWinningOption() != null) {
                lblParticipationWinner.setText("Winner: " + fullEvent.getWinningOption());
            }
        }
    }

    @FXML
    private void handleTradeAction(ActionEvent event) {
        String selectedUser = cbActiveUser.getValue();
        engine.dto.EventDTO selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        int selectedOptionIndex = cbBuyOption.getSelectionModel().getSelectedIndex();
        String sharesText = txtSharesAmount.getText();
        String priceText = txtOrderPrice.getText();

        if (selectedUser == null || selectedEvent == null || selectedOptionIndex < 0 || sharesText == null || sharesText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill in all required fields.");
            return;
        }

        try {
            int quantity = Integer.parseInt(sharesText.trim());

            // בדיקה האם האירוע הוא מסוג Order Book או LMSR
            // (בהנחה שניתן לזהות זאת דרך ה-EventDTO או סוג האירוע)
            if ("OrderBook".equals(selectedEvent.getType())) {
                double price = Double.parseDouble(priceText.trim());
                engine.models.Order.OrderType orderType = cbOrderType.getValue();
                String optionName = cbBuyOption.getValue();

                engine.executeOrderBookOrder(selectedUser, selectedEvent.getId(), optionName, orderType, price, quantity);

                showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Order Book transaction processed successfully.");
            } else {
                // לוגיקת LMSR הקודמת
                engine.dto.TradeReceiptDTO receipt = engine.buyShares(selectedUser, selectedEvent.getId(), selectedOptionIndex, quantity);
                showAlert(Alert.AlertType.INFORMATION, "Trade Successful",
                        String.format("Bought %d shares.\nCost: $%.2f\nCommission: $%.2f\nTotal Paid: $%.2f",
                                quantity, receipt.getSharesCost(), receipt.getCommission(), receipt.getTotalPaid()));
            }

            txtSharesAmount.clear();
            engine.dto.UserDTO updatedUser = engine.getAllUsers().get(selectedUser);
            if (updatedUser != null && updatedUser.getBalance() < 0) {
                showAlert(Alert.AlertType.WARNING, "Account Blocked",
                        String.format("Transaction completed, but your balance is now negative ($%.2f).\nYour account is now blocked from future actions.", updatedUser.getBalance()));
            }

            if (txtOrderPrice != null) {
                txtOrderPrice.clear();
            }

            // רענון נתונים במסך דרך רשימת המקור
            engine.dto.EventDTO updatedEvent = engine.getEventById(selectedEvent.getId());
            javafx.collections.ObservableList<engine.dto.EventDTO> sourceList =
                    (javafx.collections.ObservableList<engine.dto.EventDTO>) filteredEvents.getSource();

            int sourceIndex = sourceList.indexOf(selectedEvent);
            if (sourceIndex >= 0) {
                sourceList.set(sourceIndex, updatedEvent);
            }

            eventsTable.getSelectionModel().select(updatedEvent);
            updateTilesView();
            playPulse(tradeFormCard);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity and price must be valid numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Transaction Failed", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    public void handleOpenEvent(ActionEvent actionEvent) {
        // שליפת האירוע שנבחר מהטבלה הנכונה
        engine.dto.EventDTO selectedEvent = eventsTable.getSelectionModel().getSelectedItem();

        // שליפת המשתמש שנבחר מתיבת הטקסט הנכונה
        String selectedUser = cbActiveUser.getValue();

        if (selectedEvent == null || selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please select an event and a user first.");
            return;
        }

        try {
            // קריאה למנוע לפתיחת האירוע
            engine.openEvent(selectedEvent.getId(), selectedUser);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event opened successfully!");

            // רענון טבלת האירועים והסטטוס במסך
            // רענון טבלת האירועים והסטטוס במסך דרך רשימת המקור
            engine.dto.EventDTO updatedEvent = engine.getEventById(selectedEvent.getId());
            javafx.collections.ObservableList<engine.dto.EventDTO> sourceList =
                    (javafx.collections.ObservableList<engine.dto.EventDTO>) filteredEvents.getSource();

            int sourceIndex = sourceList.indexOf(selectedEvent);
            if (sourceIndex >= 0) {
                sourceList.set(sourceIndex, updatedEvent);
            }

            eventsTable.getSelectionModel().select(updatedEvent);
            updateTilesView(); // מוודא שגם האריחים מתעדכנים
            playPulse(tradeFormCard);

            // רענון יתרת המשתמש במסך
            updateUserDetails(selectedUser);
            engine.dto.UserDTO updatedUser = engine.getAllUsers().get(selectedUser);
            if (updatedUser != null && updatedUser.getBalance() < 0) {
                showAlert(Alert.AlertType.WARNING, "Account Blocked",
                        String.format("Event opened, but your balance is now negative ($%.2f).\nYour account is now blocked from future actions.", updatedUser.getBalance()));
            }

        } catch (engine.exception.GuessMarketException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void applyFilters() {
        if (filteredEvents == null) return;

        filteredEvents.setPredicate(event -> {
            boolean matchType = (chkTypeLMSR.isSelected() && "LMSR".equals(event.getType())) ||
                    (chkTypeOB.isSelected() && "OrderBook".equals(event.getType()));

            boolean matchStatus = (chkStatusNotActive.isSelected() && "NOT_ACTIVE".equals(event.getStatus())) ||
                    (chkStatusActive.isSelected() && "ACTIVE".equals(event.getStatus())) ||
                    (chkStatusClosed.isSelected() && "CLOSED".equals(event.getStatus()));

            boolean matchComm = (chkCommPurchase.isSelected() && "ON_PURCHASE".equals(event.getCommissionType())) ||
                    (chkCommClose.isSelected() && "ON_CLOSE".equals(event.getCommissionType()));

            return matchType && matchStatus && matchComm;
        });

        updateTilesView();
        updateEventsSummary();
    }

    private void updateEventsSummary() {
        if (lblEventsSummary == null || filteredEvents == null) return;
        int total = ((javafx.collections.ObservableList<?>) filteredEvents.getSource()).size();
        int shown = filteredEvents.size();
        lblEventsSummary.setText(shown == total
                ? shown + " event" + (shown == 1 ? "" : "s")
                : "Showing " + shown + " of " + total + " event" + (total == 1 ? "" : "s"));
    }

    @FXML
    private void handleViewChange() {
        if (btnTilesView.isSelected()) {
            eventsTable.setVisible(false);
            tilesScrollPane.setVisible(true);
            updateTilesView();
        } else {
            // מוודא שהכפתור של הטבלה יישאר לחוץ אם המשתמש מנסה לבטל בחירה
            btnTableView.setSelected(true);
            eventsTable.setVisible(true);
            tilesScrollPane.setVisible(false);
        }
    }

    private javafx.scene.layout.VBox selectedTileNode;

    private void updateTilesView() {
        if (eventsTilePane == null || filteredEvents == null) return;

        eventsTilePane.getChildren().clear();
        selectedTileNode = null;
        engine.dto.EventDTO currentlySelected = eventsTable.getSelectionModel().getSelectedItem();

        for (engine.dto.EventDTO event : filteredEvents) {
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(5);
            card.getStyleClass().addAll("card", "event-tile");
            card.setPrefWidth(220);
            card.setPrefHeight(160);
            card.setStyle("-fx-cursor: hand;"); // משנה את סמן העכבר כשעוברים על האריח

            Label nameLabel = new Label(event.getName());
            nameLabel.getStyleClass().add("text-accent");
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setWrapText(true);

            Label statusLabel = new Label("Status: " + event.getStatus());
            Label typeLabel = new Label("Type: " + event.getType());
            Label commLabel = new Label("Comm: " + event.getCommission() + "% (" + event.getCommissionType() + ")");
            Label balanceLabel = new Label(String.format("Balance: $%.2f", event.getAccountBalance()));
            balanceLabel.getStyleClass().add("text-positive");
            balanceLabel.setStyle("-fx-font-weight: bold;");

            card.getChildren().addAll(nameLabel, statusLabel, typeLabel, commLabel, balanceLabel);

            // כשלוחצים על האריח, זה מעדכן את התצוגה בימין בדיוק כמו לחיצה על שורה בטבלה, ומסמן את האריח כנבחר
            card.setOnMouseClicked(e -> {
                if (selectedTileNode != null) {
                    selectedTileNode.getStyleClass().remove("event-tile-selected");
                }
                card.getStyleClass().add("event-tile-selected");
                selectedTileNode = card;
                eventsTable.getSelectionModel().select(event);
                updateEventDetails(event);
            });

            if (currentlySelected != null && currentlySelected.getId() == event.getId()) {
                card.getStyleClass().add("event-tile-selected");
                selectedTileNode = card;
            }

            eventsTilePane.getChildren().add(card);
        }
    }

    @FXML
    public void handleCloseEvent(ActionEvent event) {
        engine.dto.EventDTO selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        String activeUser = cbActiveUser.getValue();
        int winningOptionIndex = cbWinningOption.getSelectionModel().getSelectedIndex();

        if (selectedEvent == null || activeUser == null || winningOptionIndex < 0) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please select an event, a user, and a winning option.");
            return;
        }

        // --- אישור לפני פעולה בלתי הפיכה ---
        String winningOptionName = cbWinningOption.getValue();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Close Event");
        confirm.setHeaderText(null);
        confirm.setContentText(String.format(
                "Close \"%s\" with \"%s\" as the winning option?\nThis action cannot be undone.",
                selectedEvent.getName(), winningOptionName));

        java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) {
            return; // המשתמש ביטל
        }

        try {
            engine.closeEvent(selectedEvent.getId(), winningOptionIndex);
            showAlert(Alert.AlertType.INFORMATION, "Event Closed", "The event has been successfully closed and payouts distributed.");

            engine.dto.EventDTO updatedEvent = engine.getEventById(selectedEvent.getId());
            javafx.collections.ObservableList<engine.dto.EventDTO> sourceList =
                    (javafx.collections.ObservableList<engine.dto.EventDTO>) filteredEvents.getSource();
            int sourceIndex = sourceList.indexOf(selectedEvent);
            if (sourceIndex >= 0) sourceList.set(sourceIndex, updatedEvent);
            eventsTable.getSelectionModel().select(updatedEvent);
            updateTilesView();
            updateUserDetails(activeUser);
            playPulse(tradeFormCard);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Bonus: create a brand-new event from scratch; the creating user becomes its Market Maker
    @FXML
    private void handleCreateEvent(ActionEvent actionEvent) {
        if (engine.getAllUsers().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Users Loaded", "Please load an events file first so there are users who can become the Market Maker.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Event");
        dialog.getDialogPane().getStylesheets().setAll(rootPane.getStylesheets());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> cbCreator = new ComboBox<>();
        cbCreator.getItems().addAll(engine.getAllUsers().keySet());
        cbCreator.setMaxWidth(Double.MAX_VALUE);

        TextField txtName = new TextField();
        TextArea txtDescription = new TextArea();
        txtDescription.setPrefRowCount(2);
        txtDescription.setWrapText(true);

        ComboBox<String> cbCommType = new ComboBox<>();
        cbCommType.getItems().addAll("on-purchase", "on-close");
        cbCommType.getSelectionModel().selectFirst();
        TextField txtCommission = new TextField();
        txtCommission.setPromptText("0-90");

        TextField txtOption1 = new TextField();
        TextField txtOption2 = new TextField();

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton rbLMSR = new RadioButton("LMSR");
        rbLMSR.setToggleGroup(typeGroup);
        rbLMSR.setSelected(true);
        RadioButton rbOB = new RadioButton("Order Book");
        rbOB.setToggleGroup(typeGroup);

        TextField txtB = new TextField();
        txtB.setPromptText("liquidity, e.g. 100");
        VBox lmsrFields = new VBox(6.0, new Label("Liquidity (b):"), txtB);

        TextField txtInitial = new TextField();
        txtInitial.setPromptText("initial shares, e.g. 100");
        TextField txtD = new TextField();
        txtD.setPromptText("base value, e.g. 1");
        CheckBox chkAllowMint = new CheckBox("Allow Mint");
        VBox obFields = new VBox(6.0, new Label("Initial Shares:"), txtInitial, new Label("Base Value (d):"), txtD, chkAllowMint);
        obFields.setVisible(false);
        obFields.setManaged(false);

        typeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isLMSR = newT == rbLMSR;
            lmsrFields.setVisible(isLMSR);
            lmsrFields.setManaged(isLMSR);
            obFields.setVisible(!isLMSR);
            obFields.setManaged(!isLMSR);
        });

        VBox content = new VBox(10.0,
                new Label("Market Maker (creator):"), cbCreator,
                new Label("Event Name:"), txtName,
                new Label("Description:"), txtDescription,
                new HBox(10.0, new VBox(6.0, new Label("Commission Type:"), cbCommType),
                        new VBox(6.0, new Label("Commission %:"), txtCommission)),
                new HBox(10.0, new VBox(6.0, new Label("Option 1:"), txtOption1),
                        new VBox(6.0, new Label("Option 2:"), txtOption2)),
                new HBox(15.0, rbLMSR, rbOB),
                lmsrFields, obFields
        );
        content.setPadding(new Insets(15.0));
        content.setPrefWidth(360.0);
        dialog.getDialogPane().setContent(content);

        java.util.Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            String creator = cbCreator.getValue();
            if (creator == null) {
                throw new engine.exception.GuessMarketException("Error: Please select a Market Maker.");
            }
            String name = txtName.getText() == null ? "" : txtName.getText().trim();
            String description = txtDescription.getText() == null ? "" : txtDescription.getText().trim();
            int commission = Integer.parseInt(txtCommission.getText().trim());
            CommissionType commissionType = "on-close".equals(cbCommType.getValue())
                    ? CommissionType.ON_CLOSE : CommissionType.ON_PURCHASE;
            String option1 = txtOption1.getText() == null ? "" : txtOption1.getText().trim();
            String option2 = txtOption2.getText() == null ? "" : txtOption2.getText().trim();

            int newEventId;
            if (rbLMSR.isSelected()) {
                int b = Integer.parseInt(txtB.getText().trim());
                newEventId = engine.createLmsrEvent(creator, name, description, commission, commissionType, option1, option2, b);
            } else {
                int initial = Integer.parseInt(txtInitial.getText().trim());
                int d = Integer.parseInt(txtD.getText().trim());
                newEventId = engine.createOrderBookEvent(creator, name, description, commission, commissionType,
                        option1, option2, initial, d, chkAllowMint.isSelected());
            }

            engine.dto.EventDTO newEvent = engine.getEventById(newEventId);
            if (filteredEvents != null) {
                ((javafx.collections.ObservableList<engine.dto.EventDTO>) filteredEvents.getSource()).add(newEvent);
            }
            updateTilesView();
            updateEventsSummary();

            // Take the user straight to their new event instead of leaving them to hunt for it
            eventsTable.getSelectionModel().select(newEvent);
            eventsTable.scrollTo(newEvent);
            updateEventDetails(newEvent);

            showAlert(Alert.AlertType.INFORMATION, "Event Created",
                    "\"" + name + "\" was created. " + creator + " is now its Market Maker — use the Trade / Manage panel on the right to open it.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Commission, b, initial shares and d must all be valid whole numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Could Not Create Event", e.getMessage());
        }
    }
}