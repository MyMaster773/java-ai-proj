package com.smartcanteen;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class App extends Application {
    private final DemoStore store = new DemoStore();
    private Stage stage;
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @Override public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Smart Canteen POS");
        showLaunch();
        stage.show();
    }

    private Scene scene(Parent root) {
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        return scene;
    }

    private BorderPane shell(String role, String subtitle) {
        BorderPane shell = new BorderPane(); shell.getStyleClass().add("shell");
        HBox top = new HBox(22); top.getStyleClass().add("topbar"); top.setAlignment(Pos.CENTER_LEFT);
        Label brand = new Label("SMART CANTEEN"); brand.getStyleClass().add("brand");
        Label roleLabel = new Label(role.toUpperCase(Locale.ROOT)); roleLabel.getStyleClass().add("pill");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label context = new Label(subtitle); context.getStyleClass().add("muted");
        Button home = new Button("Switch module"); home.getStyleClass().add("secondary"); home.setOnAction(e -> showLaunch());
        top.getChildren().addAll(brand, roleLabel, spacer, context, home); shell.setTop(top);
        return shell;
    }

    private void showLaunch() {
        VBox content = new VBox(28); content.setPadding(new Insets(56, 70, 50, 70)); content.setAlignment(Pos.TOP_LEFT);
        Label eyebrow = new Label("CAMPUS FOOD SERVICE"); eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("Choose a workspace"); title.getStyleClass().add("title");
        Label sub = new Label("One shared operating view for students, kitchen staff, and administrators."); sub.getStyleClass().add("subtitle");
        HBox roles = new HBox(18); roles.setMaxWidth(Double.MAX_VALUE);
        roles.getChildren().addAll(roleCard("Kiosk", "Student ordering", "Scan an ID, build a cart, and pay from the student wallet.", "Open kiosk", e -> showKiosk()), roleCard("Kitchen", "Order preparation", "Keep the queue moving from Placed to Ready to Collected.", "Open kitchen", e -> showKitchen()), roleCard("Admin", "Menu operations", "Manage availability and keep the service catalogue accurate.", "Open admin", e -> showAdmin()));
        for (Node node : roles.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);
        VBox note = new VBox(10); note.getStyleClass().add("panel");
        Label noteTitle = new Label("Prototype environment"); noteTitle.getStyleClass().add("section-title");
        Label noteText = new Label("Seeded demo data is active. Try STU-1001 for a successful order or STU-1003 to see the insufficient balance flow."); noteText.getStyleClass().add("muted"); noteText.setWrapText(true);
        HBox fakeOrders = new HBox(8); fakeOrders.setAlignment(Pos.CENTER_LEFT); Label fakeLabel = new Label("Test data: add fake orders"); fakeLabel.getStyleClass().add("item-name"); TextField fakeCount = new TextField("5"); fakeCount.setPrefWidth(70); fakeCount.getStyleClass().add("field"); Button seed = new Button("Generate orders"); seed.getStyleClass().add("secondary"); Label feedback = new Label(); feedback.getStyleClass().add("muted"); seed.setOnAction(e -> { try { int count = Integer.parseInt(fakeCount.getText().trim()); if (count < 1 || count > 100) throw new IllegalArgumentException(); store.seedFakeOrders(count); feedback.setText(count + " fake orders added."); } catch (Exception ignored) { feedback.setText("Enter a number from 1 to 100."); } }); fakeOrders.getChildren().addAll(fakeLabel, fakeCount, seed, feedback);
        note.getChildren().addAll(noteTitle, noteText, fakeOrders); content.getChildren().addAll(eyebrow, title, sub, roles, note);
        stage.setScene(scene(content));
    }

    private VBox roleCard(String title, String kicker, String copy, String action, EventHandler<ActionEvent> handler) {
        VBox box = new VBox(12); box.getStyleClass().add("role-panel"); box.setPrefHeight(210);
        Label k = new Label(kicker.toUpperCase(Locale.ROOT)); k.getStyleClass().add("eyebrow");
        Label t = new Label(title); t.getStyleClass().add("role-title"); Label c = new Label(copy); c.getStyleClass().add("role-copy"); c.setWrapText(true);
        Button b = new Button(action); b.getStyleClass().add("primary"); b.setOnAction(handler); VBox.setVgrow(c, Priority.ALWAYS);
        box.getChildren().addAll(k, t, c, b); return box;
    }

    private void showKiosk() {
        showMenu(new Student(0, "Guest checkout", "GUEST", new BigDecimal("0.00")));
    }

    private void showKioskScan() {
        BorderPane root = shell("Kiosk", "Touch-friendly student ordering");
        VBox content = new VBox(22); content.setPadding(new Insets(32, 46, 40, 46));
        Label eyebrow = new Label("ID CARD PAYMENT"); eyebrow.getStyleClass().add("eyebrow"); Label title = new Label("Enter your ID card"); title.getStyleClass().add("title");
        Label hint = new Label("Type or scan the barcode on your student card to begin."); hint.getStyleClass().add("subtitle");
        TextField scan = new TextField(); scan.setPromptText("e.g. STU-1001"); scan.getStyleClass().add("field"); scan.setMaxWidth(430);
        Label error = new Label(); error.setVisible(false); error.getStyleClass().add("error"); error.setMaxWidth(430);
        Button continueButton = new Button("Continue"); continueButton.getStyleClass().add("primary"); continueButton.setPrefWidth(150);
        Runnable submit = () -> { Student student = store.student(scan.getText().trim()); if (student == null) { error.setText("We could not find that ID. Check the code and try again."); error.setVisible(true); } else showMenu(student); };
        continueButton.setOnAction(e -> submit.run()); scan.setOnAction(e -> submit.run());
        VBox panel = new VBox(16, new Label("Student barcode"), scan, continueButton, error); panel.getStyleClass().add("panel"); panel.setMaxWidth(520);
        content.getChildren().addAll(eyebrow, title, hint, panel); root.setCenter(content); stage.setScene(scene(root)); scan.requestFocus();
    }

    private void showMenu(Student student) {
        BorderPane root = shell("Kiosk", student.name + "  /  " + student.barcode);
        VBox content = new VBox(18); content.setPadding(new Insets(26, 36, 30, 36));
        HBox heading = new HBox(16); heading.setAlignment(Pos.CENTER_LEFT); VBox headingText = new VBox(4); Label title = new Label("Choose your meal"); title.getStyleClass().add("title"); Label sub = new Label("Add items to your cart, then choose how you want to pay."); sub.getStyleClass().add("subtitle"); headingText.getChildren().addAll(title, sub); Region grow = new Region(); HBox.setHgrow(grow, Priority.ALWAYS);
        VBox balance = new VBox(5); balance.getStyleClass().add("balance"); Label bl = new Label("PAYMENT OPTIONS"); bl.getStyleClass().add("balance-label"); Label bv = new Label("UPI  /  ID card"); bv.getStyleClass().add("balance-value"); Button topUp = new Button("Top up via UPI"); topUp.getStyleClass().add("secondary"); topUp.setOnAction(e -> showTopUpDialog()); balance.getChildren().addAll(bl, bv, topUp); heading.getChildren().addAll(headingText, grow, balance);
        HBox main = new HBox(20); main.setMaxWidth(Double.MAX_VALUE); VBox menu = new VBox(12); HBox.setHgrow(menu, Priority.ALWAYS); menu.setMinWidth(0); menu.setMaxWidth(Double.MAX_VALUE);
        VBox cartBox = new VBox(14); cartBox.getStyleClass().add("panel"); cartBox.setPrefWidth(330); cartBox.setMinWidth(300); cartBox.setMaxHeight(610); cartBox.getChildren().addAll(new Label("Your cart")); cartBox.getChildren().get(0).getStyleClass().add("section-title");
        Runnable refreshCart = () -> refreshCart(cartBox, student, root);
        Label menuTitle = new Label("Today's menu"); menuTitle.getStyleClass().add("section-title"); menu.getChildren().add(menuTitle);
        Map<String, List<MenuItemModel>> grouped = store.items.stream().collect(Collectors.groupingBy(i -> i.category, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<MenuItemModel>> entry : grouped.entrySet()) { Label cat = new Label(entry.getKey()); cat.getStyleClass().add("eyebrow"); menu.getChildren().add(cat); for (MenuItemModel item : entry.getValue()) menu.getChildren().add(menuItem(item, student, refreshCart)); }
        refreshCart.run(); main.getChildren().addAll(menu, cartBox); content.getChildren().addAll(heading, main); ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); root.setCenter(scroll); stage.setScene(scene(root));
    }

    private VBox menuItem(MenuItemModel item, Student student, Runnable refreshCart) {
        HBox row = new HBox(12); row.setMinHeight(76); row.setAlignment(Pos.CENTER_LEFT); row.getStyleClass().add("menu-item"); if (!item.available) row.getStyleClass().add("menu-item-unavailable");
        VBox text = new VBox(4); text.setMinWidth(220); text.setPrefWidth(220); Label name = new Label(item.name); name.setMinWidth(200); name.setPrefWidth(200); name.setStyle("-fx-text-fill: #172033; -fx-font-size: 15px; -fx-font-weight: bold;"); Label category = new Label(item.category); category.getStyleClass().add("item-category"); text.getChildren().addAll(name, category); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label price = new Label(money(item.price)); price.getStyleClass().add("money"); Button add = new Button(item.available ? "Add" : "Unavailable"); add.getStyleClass().add(item.available ? "secondary" : "danger"); add.setDisable(!item.available); add.setOnAction(e -> { student.cart.merge(item.id, 1, Integer::sum); if (student.cart.get(item.id) > 10) student.cart.put(item.id, 10); refreshCart.run(); });
        row.getChildren().addAll(text, spacer, price, add); return new VBox(row);
    }

    private void refreshCart(VBox cartBox, Student student, BorderPane root) {
        if (cartBox.getChildren().size() > 1) cartBox.getChildren().remove(1, cartBox.getChildren().size()); BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : student.cart.entrySet()) { MenuItemModel item = store.item(entry.getKey()); if (item == null) continue; int qty = entry.getValue(); total = total.add(item.price.multiply(BigDecimal.valueOf(qty))); HBox line = new HBox(8); line.setMinWidth(260); line.setPrefWidth(300); line.setAlignment(Pos.CENTER_LEFT); Label name = new Label(item.name + " x" + qty); name.setMinWidth(120); name.setPrefWidth(120); name.setWrapText(true); name.setStyle("-fx-text-fill: #172033; -fx-font-size: 13px; -fx-font-weight: bold;"); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); Label amount = new Label(money(item.price.multiply(BigDecimal.valueOf(qty)))); Button minus = new Button("-"); minus.getStyleClass().add("secondary"); minus.setOnAction(e -> { if (qty <= 1) student.cart.remove(item.id); else student.cart.put(item.id, qty - 1); refreshCart(cartBox, student, root); }); Button plus = new Button("+"); plus.getStyleClass().add("secondary"); plus.setDisable(qty >= 10); plus.setOnAction(e -> { if (qty < 10) student.cart.put(item.id, qty + 1); refreshCart(cartBox, student, root); }); line.getChildren().addAll(name, spacer, amount, minus, plus); cartBox.getChildren().add(line); }
        BigDecimal orderTotal = total; Separator separator = new Separator(); Label totalLabel = new Label("Total   " + money(orderTotal)); totalLabel.getStyleClass().add("money"); Label status = new Label(); status.setWrapText(true); Button pay = new Button("Choose payment"); pay.getStyleClass().add("primary"); pay.setMaxWidth(Double.MAX_VALUE); pay.setOnAction(e -> choosePayment(student, orderTotal)); Button back = new Button("Start over"); back.getStyleClass().add("secondary"); back.setMaxWidth(Double.MAX_VALUE); back.setOnAction(e -> showKiosk()); cartBox.getChildren().addAll(separator, totalLabel, status, pay, back);
    }

    private void showTopUpDialog() {
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle("Top up wallet"); dialog.setHeaderText("Add balance through UPI"); ButtonType confirm = new ButtonType("Pay with UPI", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(confirm, ButtonType.CANCEL);
        TextField id = new TextField(); id.setPromptText("Student ID card, e.g. STU-1001"); TextField amount = new TextField(); amount.setPromptText("Amount"); Label current = new Label("Enter a student ID to see the current balance."); current.getStyleClass().add("muted"); id.textProperty().addListener((observable, oldValue, newValue) -> { Student student = store.student(newValue.trim()); current.setText(student == null ? "Enter a valid student ID to see the current balance." : "Current balance: " + money(student.balance)); }); VBox content = new VBox(10, new Label("Student ID card"), id, current, new Label("Top-up amount"), amount); content.setPadding(new Insets(8)); dialog.getDialogPane().setContent(content);
        Optional<ButtonType> result = dialog.showAndWait(); if (result.isEmpty() || result.get() != confirm) return;
        try { Student student = store.student(id.getText().trim()); BigDecimal topUp = new BigDecimal(amount.getText()).setScale(2); if (student == null || topUp.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(); student.balance = student.balance.add(topUp); showAlert("Top up successful", student.name + "'s new balance is " + money(student.balance) + "."); } catch (Exception ignored) { showAlert("Top up failed", "Enter a valid student ID and a positive amount."); }
    }

    private void choosePayment(Student cartOwner, BigDecimal total) {
        if (cartOwner.cart.isEmpty()) return;
        for (Integer id : cartOwner.cart.keySet()) if (!store.item(id).available) {
            showAlert("Item unavailable", "An item in your cart is no longer available. Remove it and try again."); return;
        }
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle("Payment method"); dialog.setHeaderText("How would you like to pay?");
        ButtonType confirm = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(confirm, ButtonType.CANCEL);
        RadioButton upi = new RadioButton("UPI"); RadioButton idCard = new RadioButton("ID card"); upi.setSelected(true); ToggleGroup methods = new ToggleGroup(); upi.setToggleGroup(methods); idCard.setToggleGroup(methods);
        TextField idField = new TextField(); idField.setPromptText("e.g. STU-1001"); idField.setVisible(false); idField.setManaged(false);
        Label idHint = new Label("ID card details are required only for ID card payment."); idHint.getStyleClass().add("muted"); idHint.setVisible(false); idHint.setManaged(false);
        Label error = new Label(); error.getStyleClass().add("error"); error.setVisible(false); error.setManaged(false);
        Runnable updateFields = () -> { boolean card = idCard.isSelected(); idField.setVisible(card); idField.setManaged(card); idHint.setVisible(card); idHint.setManaged(card); };
        upi.setOnAction(e -> updateFields.run()); idCard.setOnAction(e -> updateFields.run());
        VBox content = new VBox(12, new Label("Order total: " + money(total)), upi, idCard, idField, idHint, error); content.setPadding(new Insets(8)); dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().lookupButton(confirm).addEventFilter(ActionEvent.ACTION, event -> {
            if (idCard.isSelected() && store.student(idField.getText().trim()) == null) { error.setText("Enter a valid student ID card code."); error.setVisible(true); error.setManaged(true); event.consume(); }
        });
        Optional<ButtonType> result = dialog.showAndWait(); if (result.isEmpty() || result.get() != confirm) return;
        Student payer = idCard.isSelected() ? store.student(idField.getText().trim()) : cartOwner;
        if (idCard.isSelected() && payer.balance.compareTo(total) < 0) { showAlert("Insufficient balance", "This ID card is short by " + money(total.subtract(payer.balance)) + ". Your cart is still saved."); return; }
        if (idCard.isSelected()) payer.balance = payer.balance.subtract(total);
        Order order = store.createOrder(payer, total, cartOwner.cart, idCard.isSelected() ? "ID card" : "UPI"); cartOwner.cart.clear(); showReceipt(payer, order);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK); alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }

    private void showReceipt(Student student, Order order) {
        BorderPane root = shell("Kiosk", "Payment complete"); VBox box = new VBox(18); box.setPadding(new Insets(34, 50, 40, 50)); Label eyebrow = new Label("ORDER CONFIRMED"); eyebrow.getStyleClass().add("eyebrow"); Label title = new Label("Thanks, " + student.name.split(" ")[0]); title.getStyleClass().add("title"); Label token = new Label("Token #" + order.token); token.getStyleClass().add("money"); VBox receipt = new VBox(12); receipt.getStyleClass().add("panel"); receipt.getChildren().addAll(new Label("Receipt"), new Label("Placed " + order.created.format(timeFormat)), new Label("Payment method  " + order.paymentMethod)); receipt.getChildren().get(0).getStyleClass().add("section-title"); for (CartLine line : order.lines) receipt.getChildren().add(new Label(line.item.name + "  x" + line.quantity + "  " + money(line.item.price.multiply(BigDecimal.valueOf(line.quantity))))); receipt.getChildren().addAll(new Separator(), new Label("Total paid  " + money(order.total))); if (order.paymentMethod.equals("ID card")) receipt.getChildren().add(new Label("Balance after payment  " + money(student.balance))); Label resetNote = new Label("Returning to ordering in 5 seconds..."); resetNote.getStyleClass().add("muted"); Button save = new Button("Save receipt as text"); save.getStyleClass().add("primary"); save.setOnAction(e -> saveReceipt(order, student)); Button newOrder = new Button("Start next order"); newOrder.getStyleClass().add("secondary"); newOrder.setOnAction(e -> showKiosk()); box.getChildren().addAll(eyebrow, title, token, receipt, save, resetNote, newOrder); root.setCenter(box); stage.setScene(scene(root)); Timeline reset = new Timeline(new KeyFrame(Duration.seconds(5), e -> showKiosk())); reset.play();
    }

    private void saveReceipt(Order order, Student student) { FileChooser chooser = new FileChooser(); chooser.setInitialFileName("canteen-token-" + order.token + ".txt"); File file = chooser.showSaveDialog(stage); if (file == null) return; try (FileWriter writer = new FileWriter(file)) { writer.write("SMART CANTEEN\nToken #" + order.token + "\n" + order.created.format(timeFormat) + "\nPayment method: " + order.paymentMethod + "\n\n"); for (CartLine line : order.lines) writer.write(line.item.name + " x" + line.quantity + "\n"); writer.write("\nTotal: " + money(order.total)); if (order.paymentMethod.equals("ID card")) writer.write("\nBalance after payment: " + money(student.balance)); } catch (IOException ignored) { } }

    private void showKitchen() {
        BorderPane root = shell("Kitchen", "Live preparation queue"); VBox content = new VBox(18); content.setPadding(new Insets(28, 38, 30, 38)); HBox header = new HBox(12); Label title = new Label("Kitchen queue"); title.getStyleClass().add("title"); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); Label live = new Label("LIVE · polling every 3 seconds"); live.getStyleClass().add("pill"); header.getChildren().addAll(title, spacer, live); FlowPane orders = new FlowPane(16, 16); orders.setPrefWrapLength(1060); Runnable refresh = () -> refreshKitchen(orders); refresh.run(); Timeline timer = new Timeline(new KeyFrame(Duration.seconds(3), e -> refresh.run())); timer.setCycleCount(Timeline.INDEFINITE); timer.play(); content.getChildren().addAll(header, new Label("Oldest orders first. Move each order forward as it is prepared."), orders); ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); root.setCenter(scroll); stage.setScene(scene(root)); }

    private void refreshKitchen(FlowPane orders) { orders.getChildren().clear(); for (Order order : store.orders) if (!order.status.equals("Collected")) { VBox card = new VBox(10); card.getStyleClass().add("panel"); card.setPrefWidth(335); HBox top = new HBox(8); Label token = new Label("#" + order.token); token.getStyleClass().add("title"); Label status = new Label(order.status); status.getStyleClass().addAll("pill", "status-" + order.status.toLowerCase()); top.getChildren().addAll(token, status); card.getChildren().addAll(top, new Label(order.student.name + " · " + order.created.format(DateTimeFormatter.ofPattern("HH:mm")))); for (CartLine line : order.lines) card.getChildren().add(new Label(line.quantity + " x " + line.item.name)); Button next = new Button(nextStatus(order.status)); next.getStyleClass().add("primary"); next.setOnAction(e -> { order.status = nextStatusValue(order.status); refreshKitchen(orders); }); card.getChildren().add(next); orders.getChildren().add(card); } if (orders.getChildren().isEmpty()) { Label empty = new Label("No active orders. New paid orders will appear here automatically."); empty.getStyleClass().add("subtitle"); orders.getChildren().add(empty); } }

    private String nextStatus(String status) { return switch (status) { case "Placed" -> "Start preparing"; case "Preparing" -> "Mark ready"; default -> "Mark collected"; }; }
    private String nextStatusValue(String status) { return switch (status) { case "Placed" -> "Preparing"; case "Preparing" -> "Ready"; default -> "Collected"; }; }

    private void showAdmin() {
        BorderPane root = shell("Admin", "Menu, sales, and student accounts");
        VBox content = new VBox(18); content.setPadding(new Insets(28, 38, 30, 38));
        Label title = new Label("Admin workspace"); title.getStyleClass().add("title");
        Label sub = new Label("Keep the catalogue accurate, review today's performance, and inspect student wallet balances."); sub.getStyleClass().add("subtitle");
        TabPane tabs = new TabPane(); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab menuTab = new Tab("Menu management", tabScroll(menuManagement()));
        Tab salesTab = new Tab("Today's sales", tabScroll(salesSummary()));
        Tab studentsTab = new Tab("Students", tabScroll(studentDirectory()));
        tabs.getTabs().addAll(menuTab, salesTab, studentsTab); VBox.setVgrow(tabs, Priority.ALWAYS);
        content.getChildren().addAll(title, sub, tabs); ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); root.setCenter(scroll); stage.setScene(scene(root));
    }

    private ScrollPane tabScroll(Parent content) { ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); return scroll; }

    private VBox menuManagement() {
        VBox wrapper = new VBox(14); wrapper.setPadding(new Insets(18, 4, 4, 4));
        Label heading = new Label("Catalogue"); heading.getStyleClass().add("section-title");
        HBox form = new HBox(8); form.getStyleClass().add("panel");
        TextField name = new TextField(); name.setPromptText("Item name"); name.getStyleClass().add("field");
        ComboBox<String> category = new ComboBox<>(); category.getItems().addAll("Snacks", "Meals", "Beverages", "Desserts"); category.setPromptText("Category"); category.getStyleClass().add("field"); category.setPrefWidth(150);
        TextField price = new TextField(); price.setPromptText("Price"); price.getStyleClass().add("field");
        Button add = new Button("Add item"); add.getStyleClass().add("primary"); Label feedback = new Label(); feedback.getStyleClass().add("muted");
        add.setOnAction(e -> { try { if (name.getText().isBlank() || category.getValue() == null) throw new IllegalArgumentException(); store.items.add(new MenuItemModel(store.nextItemId++, name.getText().trim(), category.getValue(), price.getText().trim(), true)); showAdmin(); } catch (Exception ignored) { feedback.setText("Enter a name, choose a category, and enter a valid price."); } });
        form.getChildren().addAll(name, category, price, add); HBox.setHgrow(name, Priority.ALWAYS); HBox.setHgrow(category, Priority.ALWAYS); HBox.setHgrow(price, Priority.ALWAYS);
        VBox list = new VBox(0); list.getStyleClass().add("panel");
        for (MenuItemModel item : store.items) {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT); row.getStyleClass().add("table-row-cell");
            VBox labels = new VBox(3); Label itemName = new Label(item.name); itemName.getStyleClass().add("item-name"); Label detail = new Label(item.category); detail.getStyleClass().add("muted"); labels.getChildren().addAll(itemName, detail);
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); TextField editPrice = new TextField(item.price.toPlainString()); editPrice.setPrefWidth(90); editPrice.getStyleClass().add("field");
            Button savePrice = new Button("Save price"); savePrice.getStyleClass().add("secondary"); savePrice.setOnAction(e -> { try { item.price = new BigDecimal(editPrice.getText()).setScale(2); showAdmin(); } catch (NumberFormatException ignored) { editPrice.setText(item.price.toPlainString()); } });
            Label state = new Label(item.available ? "Available" : "Unavailable"); state.getStyleClass().addAll("pill", item.available ? "status-ready" : "status-placed"); Button toggle = new Button(item.available ? "Disable" : "Enable"); toggle.getStyleClass().add("secondary"); toggle.setOnAction(e -> { item.available = !item.available; showAdmin(); });
            Button delete = new Button("Delete"); delete.getStyleClass().add("danger"); delete.setOnAction(e -> { store.items.remove(item); showAdmin(); }); row.getChildren().addAll(labels, spacer, editPrice, savePrice, state, toggle, delete); list.getChildren().add(row);
        }
        wrapper.getChildren().addAll(heading, form, feedback, list); VBox.setVgrow(list, Priority.ALWAYS); return wrapper;
    }

    private VBox salesSummary() {
        VBox wrapper = new VBox(16); wrapper.setPadding(new Insets(18, 4, 4, 4));
        List<Order> today = store.orders.stream().filter(o -> o.created.toLocalDate().equals(LocalDateTime.now().toLocalDate())).toList();
        long ordersToday = today.size();
        BigDecimal receivedToday = today.stream().map(o -> o.total).reduce(BigDecimal.ZERO, BigDecimal::add);
        HBox metrics = new HBox(14); metrics.getChildren().addAll(metric("Orders today", String.valueOf(ordersToday)), metric("Total payments received", money(receivedToday)), metric("Active orders", String.valueOf(store.orders.stream().filter(o -> !o.status.equals("Collected")).count())));
        Label chartsHeading = new Label("Sales analytics"); chartsHeading.getStyleClass().add("section-title");
        HBox charts = new HBox(14); charts.getChildren().addAll(paymentChart(today), categoryChart(today));
        HBox charts2 = new HBox(14); charts2.getChildren().addAll(hourlyChart(today), statusChart(today));
        Label detailHeading = new Label("Transaction details"); detailHeading.getStyleClass().add("section-title"); VBox table = new VBox(0); table.getStyleClass().add("panel");
        if (today.isEmpty()) { Label empty = new Label("No payments received today."); empty.getStyleClass().add("muted"); table.getChildren().add(empty); } else for (Order order : today) { HBox row = new HBox(14); row.getStyleClass().add("table-row-cell"); Label token = new Label("Token #" + order.token); token.getStyleClass().add("item-name"); Label student = new Label(order.student.name); Label method = new Label(order.paymentMethod); method.getStyleClass().add("muted"); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); Label amount = new Label(money(order.total)); amount.getStyleClass().add("money"); row.getChildren().addAll(token, student, method, spacer, amount); table.getChildren().add(row); }
        wrapper.getChildren().addAll(metrics, chartsHeading, charts, charts2, detailHeading, table); return wrapper;
    }

    private PieChart paymentChart(List<Order> orders) { PieChart chart = new PieChart(); chart.setTitle("Payments by method"); chart.setLegendVisible(true); Map<String, BigDecimal> totals = new LinkedHashMap<>(); for (Order order : orders) totals.merge(order.paymentMethod, order.total, BigDecimal::add); for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) chart.getData().add(new PieChart.Data(entry.getKey() + " (" + money(entry.getValue()) + ")", entry.getValue().doubleValue())); chart.setPrefSize(400, 280); return chart; }

    private BarChart<String, Number> categoryChart(List<Order> orders) { CategoryAxis x = new CategoryAxis(); NumberAxis y = new NumberAxis(); x.setLabel("Category"); y.setLabel("Amount"); BarChart<String, Number> chart = new BarChart<>(x, y); chart.setTitle("Sales by category"); XYChart.Series<String, Number> series = new XYChart.Series<>(); series.setName("Revenue"); Map<String, BigDecimal> totals = new LinkedHashMap<>(); for (Order order : orders) for (CartLine line : order.lines) totals.merge(line.item.category, line.item.price.multiply(BigDecimal.valueOf(line.quantity)), BigDecimal::add); for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue())); chart.getData().add(series); chart.setLegendVisible(false); chart.setPrefSize(450, 280); return chart; }

    private LineChart<String, Number> hourlyChart(List<Order> orders) { CategoryAxis x = new CategoryAxis(); NumberAxis y = new NumberAxis(); x.setLabel("Hour"); y.setLabel("Payments"); LineChart<String, Number> chart = new LineChart<>(x, y); chart.setTitle("Payments by hour"); XYChart.Series<String, Number> series = new XYChart.Series<>(); series.setName("Orders"); Map<Integer, BigDecimal> hours = new TreeMap<>(); for (Order order : orders) hours.merge(order.created.getHour(), order.total, BigDecimal::add); for (Map.Entry<Integer, BigDecimal> entry : hours.entrySet()) series.getData().add(new XYChart.Data<>(String.format("%02d:00", entry.getKey()), entry.getValue().doubleValue())); chart.getData().add(series); chart.setLegendVisible(false); chart.setPrefSize(450, 280); return chart; }

    private PieChart statusChart(List<Order> orders) { PieChart chart = new PieChart(); chart.setTitle("Orders by status"); Map<String, Long> statuses = orders.stream().collect(Collectors.groupingBy(o -> o.status, LinkedHashMap::new, Collectors.counting())); for (Map.Entry<String, Long> entry : statuses.entrySet()) chart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue())); chart.setPrefSize(400, 280); return chart; }

    private VBox metric(String label, String value) { VBox box = new VBox(5); box.getStyleClass().add("panel"); Label v = new Label(value); v.getStyleClass().add("money"); Label l = new Label(label); l.getStyleClass().add("muted"); box.getChildren().addAll(v, l); HBox.setHgrow(box, Priority.ALWAYS); return box; }

    private VBox studentDirectory() {
        VBox wrapper = new VBox(16); wrapper.setPadding(new Insets(18, 4, 4, 4)); Label heading = new Label("Student wallet balances"); heading.getStyleClass().add("section-title");
        HBox addForm = new HBox(8); addForm.getStyleClass().add("panel"); TextField name = new TextField(); name.setPromptText("Student name"); name.getStyleClass().add("field"); TextField barcode = new TextField(); barcode.setPromptText("ID card code"); barcode.getStyleClass().add("field"); TextField balance = new TextField(); balance.setPromptText("Starting balance"); balance.getStyleClass().add("field"); Button add = new Button("Add student"); add.getStyleClass().add("primary"); Label feedback = new Label(); feedback.getStyleClass().add("muted");
        add.setOnAction(e -> { try { if (name.getText().isBlank() || barcode.getText().isBlank()) throw new IllegalArgumentException(); if (store.student(barcode.getText().trim()) != null) throw new IllegalStateException(); store.students.add(new Student(store.nextStudentId++, name.getText().trim(), barcode.getText().trim(), new BigDecimal(balance.getText()).setScale(2))); showAdmin(); } catch (Exception ignored) { feedback.setText("Enter a unique ID card code and a valid balance."); } });
        addForm.getChildren().addAll(name, barcode, balance, add); HBox.setHgrow(name, Priority.ALWAYS); HBox.setHgrow(barcode, Priority.ALWAYS); HBox.setHgrow(balance, Priority.ALWAYS);
        VBox table = new VBox(0); table.getStyleClass().add("panel");
        for (Student student : store.students) { HBox row = new HBox(10); row.getStyleClass().add("table-row-cell"); Label studentName = new Label(student.name); studentName.getStyleClass().add("item-name"); Label studentBarcode = new Label(student.barcode); studentBarcode.getStyleClass().add("muted"); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); TextField editBalance = new TextField(student.balance.toPlainString()); editBalance.setPrefWidth(110); editBalance.getStyleClass().add("field"); Button save = new Button("Set balance"); save.getStyleClass().add("secondary"); save.setOnAction(e -> { try { student.balance = new BigDecimal(editBalance.getText()).setScale(2); showAdmin(); } catch (NumberFormatException ignored) { editBalance.setText(student.balance.toPlainString()); } }); row.getChildren().addAll(studentName, studentBarcode, spacer, new Label("Remaining amount"), editBalance, save); table.getChildren().add(row); }
        wrapper.getChildren().addAll(heading, addForm, feedback, table); return wrapper;
    }

    private String money(BigDecimal value) { return "Rs " + value.setScale(2).toPlainString(); }

    private static class Student { final int id; final String name, barcode; BigDecimal balance; final Map<Integer,Integer> cart = new LinkedHashMap<>(); Student(int id, String name, String barcode, BigDecimal balance) { this.id=id; this.name=name; this.barcode=barcode; this.balance=balance; } }
    private static class MenuItemModel { final int id; final String name, category; BigDecimal price; boolean available; MenuItemModel(int id, String name, String category, String price, boolean available) { this.id=id; this.name=name; this.category=category; this.price=new BigDecimal(price); this.available=available; } }
    private static class CartLine { final MenuItemModel item; final int quantity; CartLine(MenuItemModel item, int quantity) { this.item=item; this.quantity=quantity; } }
    private static class Order { final int token; final Student student; final BigDecimal total; LocalDateTime created; final List<CartLine> lines; final String paymentMethod; String status="Placed"; Order(int token, Student student, BigDecimal total, List<CartLine> lines, String paymentMethod) { this.token=token; this.student=student; this.total=total; this.lines=lines; this.paymentMethod=paymentMethod; this.created=LocalDateTime.now(); } }
    private static class DemoStore { final List<Student> students = new ArrayList<>(); final List<MenuItemModel> items = new ArrayList<>(); final List<Order> orders = new ArrayList<>(); int token = 1; int nextItemId = 9; int nextStudentId = 4; DemoStore() { students.add(new Student(1,"Aarav Sharma","STU-1001",new BigDecimal("480.00"))); students.add(new Student(2,"Meera Nair","STU-1002",new BigDecimal("220.00"))); students.add(new Student(3,"Kabir Singh","STU-1003",new BigDecimal("35.00"))); items.add(new MenuItemModel(1,"Masala dosa","Meals","65.00",true)); items.add(new MenuItemModel(2,"Veg thali","Meals","120.00",true)); items.add(new MenuItemModel(3,"Paneer wrap","Meals","95.00",true)); items.add(new MenuItemModel(4,"Samosa","Snacks","25.00",true)); items.add(new MenuItemModel(5,"Idli sambar","Snacks","55.00",true)); items.add(new MenuItemModel(6,"Lemon tea","Beverages","20.00",true)); items.add(new MenuItemModel(7,"Cold coffee","Beverages","70.00",true)); items.add(new MenuItemModel(8,"Gulab jamun","Desserts","45.00",false)); } Student student(String barcode) { return students.stream().filter(s -> s.barcode.equalsIgnoreCase(barcode)).findFirst().orElse(null); } MenuItemModel item(int id) { return items.stream().filter(i -> i.id == id).findFirst().orElse(null); } Order createOrder(Student student, BigDecimal total, Map<Integer,Integer> cart, String paymentMethod) { List<CartLine> lines = cart.entrySet().stream().map(e -> new CartLine(item(e.getKey()),e.getValue())).toList(); Order order = new Order(token++, student, total, lines, paymentMethod); orders.add(order); return order; } void seedFakeOrders(int count) { Random random = new Random(); List<MenuItemModel> available = items.stream().filter(i -> i.available).toList(); String[] statuses = {"Placed", "Preparing", "Ready"}; for (int i = 0; i < count; i++) { Student student = students.get(random.nextInt(students.size())); Map<Integer,Integer> cart = new LinkedHashMap<>(); int itemCount = 1 + random.nextInt(Math.min(3, available.size())); while (cart.size() < itemCount) { MenuItemModel item = available.get(random.nextInt(available.size())); cart.put(item.id, 1 + random.nextInt(2)); } BigDecimal total = cart.entrySet().stream().map(e -> item(e.getKey()).price.multiply(BigDecimal.valueOf(e.getValue()))).reduce(BigDecimal.ZERO, BigDecimal::add); Order order = createOrder(student, total, cart, random.nextBoolean() ? "UPI" : "ID card"); order.created = LocalDateTime.now().minusMinutes(random.nextInt(12 * 60)); order.status = statuses[random.nextInt(statuses.length)]; } } }
}
