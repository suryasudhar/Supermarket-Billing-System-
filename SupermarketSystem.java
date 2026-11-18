import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class SupermarketSystem extends JFrame {

    // --- Color Palette (Matching Web App CSS) ---
    private static final Color COLOR_PRIMARY = new Color(23, 107, 82);     // #176b52
    private static final Color COLOR_SECONDARY = new Color(100, 116, 139); // #64748b
    private static final Color COLOR_DANGER = new Color(220, 53, 69);      // #dc3545
    private static final Color COLOR_BG = new Color(243, 246, 251);        // #f3f6fb
    private static final Color COLOR_PANEL_BG = Color.WHITE;
    private static final Color COLOR_TEXT = new Color(31, 41, 55);

    private static final DecimalFormat MONEY_FMT = new DecimalFormat("0.00");
    private static final double GST_RATE = 0.05;
    private static final double DISCOUNT_LIMIT = 1000.0;
    private static final double DISCOUNT_RATE = 0.03;

    // --- Data Models ---
    static class Product {
        String name;
        double price;
        Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
        @Override
        public String toString() {
            return name + " — Rs. " + MONEY_FMT.format(price);
        }
    }

    static class CartItem {
        Product product;
        int quantity;
        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        double getAmount() {
            return product.price * quantity;
        }
    }

    static class CalendarEvent {
        String title;
        String description;
        LocalDateTime dateTime;
        String type; // Reminder, Shift, Sale, Meeting

        CalendarEvent(String title, String description, LocalDateTime dateTime, String type) {
            this.title = title;
            this.description = description;
            this.dateTime = dateTime;
            this.type = type;
        }
    }

    private final List<Product> productCatalog = new ArrayList<>();
    private final Map<Integer, CartItem> cart = new LinkedHashMap<>();
    private final List<CalendarEvent> eventsList = new ArrayList<>();

    // --- Layout & Components ---
    private CardLayout cardLayout;
    private JPanel mainCardsPanel;

    // State Variables
    private String currentCashier = "";
    private String currentCounter = "";

    // Billing UI Elements
    private JLabel staffInfoLabel;
    private JComboBox<Product> productDropdown;
    private JSpinner quantitySpinner;
    private DefaultTableModel billTableModel;
    private JLabel subtotalLabel, gstLabel, discountLabel, grandTotalLabel;

    // Receipt UI Elements
    private JTextArea receiptTextArea;

    // Calendar UI Elements
    private YearMonth currentYearMonth = YearMonth.now();
    private JPanel daysGridPanel;
    private JLabel monthYearLabel;
    private DefaultListModel<String> dayAgendaModel;
    private LocalDate selectedDate = LocalDate.now();

    public SupermarketSystem() {
        initData();
        initUI();
    }

    private void initData() {
        productCatalog.add(new Product("Rice (1 kg)", 62.00));
        productCatalog.add(new Product("Wheat Flour (1 kg)", 48.00));
        productCatalog.add(new Product("Sugar (1 kg)", 44.00));
        productCatalog.add(new Product("Toor Dal (1 kg)", 135.00));
        productCatalog.add(new Product("Sunflower Oil (1 L)", 148.00));
        productCatalog.add(new Product("Milk (500 ml)", 28.00));
        productCatalog.add(new Product("Tea Powder (250 g)", 120.00));
        productCatalog.add(new Product("Biscuits Pack", 25.00));
        productCatalog.add(new Product("Soap Bar", 38.00));
        productCatalog.add(new Product("Shampoo Bottle", 180.00));

        // Sample Events
        eventsList.add(new CalendarEvent("Weekly Stock Inventory", "Audit grain stock and beverages", LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), "Task"));
        eventsList.add(new CalendarEvent("Staff Shift Briefing", "Morning counter assignment", LocalDateTime.now().withHour(8).withMinute(30), "Shift"));
        eventsList.add(new CalendarEvent("Weekend Discount Promotion", "Apply 5% extra discount on bulk items", LocalDateTime.now().plusDays(3).withHour(9).withMinute(0), "Sale"));
    }

    private void initUI() {
        setTitle("Supermarket Billing & Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel headerTitle = new JLabel("Supermarket Billing System", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Arial", Font.BOLD, 22));
        headerTitle.setForeground(Color.WHITE);
        headerPanel.add(headerTitle, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Card Container
        cardLayout = new CardLayout();
        mainCardsPanel = new JPanel(cardLayout);
        mainCardsPanel.setBackground(COLOR_BG);
        mainCardsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainCardsPanel.add(createHomePage(), "HOME");
        mainCardsPanel.add(createBillingPage(), "BILLING");
        mainCardsPanel.add(createReceiptPage(), "RECEIPT");
        mainCardsPanel.add(createCalendarPage(), "CALENDAR");

        add(mainCardsPanel, BorderLayout.CENTER);
        cardLayout.show(mainCardsPanel, "HOME");
    }

    // Helper: Styled Custom Button
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 14, 8, 14));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    // ==========================================
    // 1. HOME PAGE
    // ==========================================
    private JPanel createHomePage() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(30, 30, 30, 30)
        ));
        panel.setPreferredSize(new Dimension(480, 380));

        JLabel title = new JLabel("Start a new bill");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cashierLabel = new JLabel("Cashier name");
        cashierLabel.setFont(new Font("Arial", Font.BOLD, 13));
        cashierLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField cashierField = new JTextField();
        cashierField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cashierField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel counterLabel = new JLabel("Select counter");
        counterLabel.setFont(new Font("Arial", Font.BOLD, 13));
        counterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> counterSelect = new JComboBox<>(new String[]{"Counter 1", "Counter 2", "Counter 3"});
        counterSelect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        counterSelect.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton startBtn = createStyledButton("Start Billing", COLOR_PRIMARY);
        startBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        startBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton calBtn = createStyledButton("Open Calendar & Schedule", COLOR_SECONDARY);
        calBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        calBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        startBtn.addActionListener(e -> {
            String name = cashierField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the cashier name.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            currentCashier = name;
            currentCounter = (String) counterSelect.getSelectedItem();
            staffInfoLabel.setText("Cashier: " + currentCashier + "  |  " + currentCounter);
            cart.clear();
            renderBill();
            cardLayout.show(mainCardsPanel, "BILLING");
        });

        calBtn.addActionListener(e -> {
            updateCalendarDisplay();
            cardLayout.show(mainCardsPanel, "CALENDAR");
        });

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(cashierLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cashierField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(counterLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(counterSelect);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(startBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(calBtn);

        wrapper.add(panel);
        return wrapper;
    }

    // ==========================================
    // 2. BILLING PAGE
    // ==========================================
    private JPanel createBillingPage() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(COLOR_PANEL_BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Section: Info + Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(COLOR_PANEL_BG);

        JLabel pageTitle = new JLabel("Billing");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 22));
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        staffInfoLabel = new JLabel("Cashier: - | Counter 1");
        staffInfoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        staffInfoLabel.setForeground(COLOR_SECONDARY);
        staffInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Toolbar Panel
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        toolbar.setBackground(COLOR_PANEL_BG);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        productDropdown = new JComboBox<>(productCatalog.toArray(new Product[0]));
        productDropdown.setPreferredSize(new Dimension(280, 36));

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setPreferredSize(new Dimension(80, 36));

        JButton addBtn = createStyledButton("Add to Bill", COLOR_PRIMARY);

        toolbar.add(new JLabel("Product:"));
        toolbar.add(productDropdown);
        toolbar.add(new JLabel("Qty:"));
        toolbar.add(quantitySpinner);
        toolbar.add(addBtn);

        topContainer.add(pageTitle);
        topContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        topContainer.add(staffInfoLabel);
        topContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        topContainer.add(toolbar);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"S.No", "Item", "Price (Rs.)", "Qty", "Amount (Rs.)", "Action"};
        billTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only action button column interactive
            }
        };

        JTable table = new JTable(billTableModel);
        table.setRowHeight(36);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 250, 252));

        // Right alignment for numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        // Action Column Renderer/Editor (Remove Button)
        table.getColumnModel().getColumn(5).setCellRenderer((t, val, isSel, hasFocus, row, col) -> {
            JButton btn = new JButton("Remove");
            btn.setBackground(COLOR_DANGER);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            return btn;
        });

        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JButton btn = new JButton("Remove");
            private int currentRow;

            {
                btn.setBackground(COLOR_DANGER);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.addActionListener(e -> {
                    fireEditingStopped();
                    cart.remove(currentRow);
                    renderBill();
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
                this.currentRow = row;
                return btn;
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        // Bottom Section: Totals & Action Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(COLOR_PANEL_BG);

        // Totals Box
        JPanel totalsPanel = new JPanel(new GridLayout(4, 2, 10, 6));
        totalsPanel.setBackground(COLOR_PANEL_BG);
        totalsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        subtotalLabel = new JLabel("0.00", SwingConstants.RIGHT);
        gstLabel = new JLabel("0.00", SwingConstants.RIGHT);
        discountLabel = new JLabel("0.00", SwingConstants.RIGHT);
        grandTotalLabel = new JLabel("0.00", SwingConstants.RIGHT);
        grandTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("GST (5%):"));
        totalsPanel.add(gstLabel);
        totalsPanel.add(new JLabel("Discount:"));
        totalsPanel.add(discountLabel);
        JLabel gtTitle = new JLabel("Grand Total:");
        gtTitle.setFont(new Font("Arial", Font.BOLD, 16));
        totalsPanel.add(gtTitle);
        totalsPanel.add(grandTotalLabel);

        JPanel totalsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalsWrapper.setBackground(COLOR_PANEL_BG);
        totalsWrapper.setPreferredSize(new Dimension(320, 130));
        totalsWrapper.add(totalsPanel);

        // Action Buttons Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionsPanel.setBackground(COLOR_PANEL_BG);

        JButton clearBtn = createStyledButton("Clear Bill", COLOR_SECONDARY);
        JButton homeBtn = createStyledButton("Back to Home", COLOR_SECONDARY);
        JButton calBtn = createStyledButton("Calendar", COLOR_SECONDARY);
        JButton generateBtn = createStyledButton("Generate Bill", COLOR_PRIMARY);

        actionsPanel.add(clearBtn);
        actionsPanel.add(homeBtn);
        actionsPanel.add(calBtn);
        actionsPanel.add(generateBtn);

        bottomPanel.add(totalsWrapper, BorderLayout.NORTH);
        bottomPanel.add(actionsPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Event Handlers
        addBtn.addActionListener(e -> {
            int idx = productDropdown.getSelectedIndex();
            int qty = (int) quantitySpinner.getValue();
            Product p = productCatalog.get(idx);

            if (cart.containsKey(idx)) {
                CartItem existing = cart.get(idx);
                if (existing.quantity + qty > 100) {
                    JOptionPane.showMessageDialog(this, "The maximum quantity for an item is 100.");
                    return;
                }
                existing.quantity += qty;
            } else {
                cart.put(idx, new CartItem(p, qty));
            }
            quantitySpinner.setValue(1);
            renderBill();
        });

        clearBtn.addActionListener(e -> {
            cart.clear();
            renderBill();
        });

        homeBtn.addActionListener(e -> cardLayout.show(mainCardsPanel, "HOME"));
        calBtn.addActionListener(e -> {
            updateCalendarDisplay();
            cardLayout.show(mainCardsPanel, "CALENDAR");
        });

        generateBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The bill is empty. Add an item before generating a bill.");
                return;
            }
            buildReceiptText();
            cardLayout.show(mainCardsPanel, "RECEIPT");
        });

        return mainPanel;
    }

    private void renderBill() {
        billTableModel.setRowCount(0);
        double subtotal = 0.0;
        int sNo = 1;

        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            CartItem item = entry.getValue();
            subtotal += item.getAmount();
            billTableModel.addRow(new Object[]{
                    sNo++,
                    item.product.name,
                    MONEY_FMT.format(item.product.price),
                    item.quantity,
                    MONEY_FMT.format(item.getAmount()),
                    "Remove"
            });
        }

        double gst = subtotal * GST_RATE;
        double discount = (subtotal >= DISCOUNT_LIMIT) ? (subtotal * DISCOUNT_RATE) : 0.0;
        double grandTotal = subtotal + gst - discount;

        subtotalLabel.setText("Rs. " + MONEY_FMT.format(subtotal));
        gstLabel.setText("Rs. " + MONEY_FMT.format(gst));
        discountLabel.setText("Rs. " + MONEY_FMT.format(discount));
        grandTotalLabel.setText("Rs. " + MONEY_FMT.format(grandTotal));
    }

    // ==========================================
    // 3. RECEIPT PAGE
    // ==========================================
    private JPanel createReceiptPage() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Receipt");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(title, BorderLayout.NORTH);

        receiptTextArea = new JTextArea();
        receiptTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptTextArea.setEditable(false);
        receiptTextArea.setBackground(new Color(251, 253, 255));
        receiptTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(148, 163, 184)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        panel.add(new JScrollPane(receiptTextArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(COLOR_PANEL_BG);

        JButton newBillBtn = createStyledButton("New Bill", COLOR_PRIMARY);
        JButton backHomeBtn = createStyledButton("Back to Home", COLOR_SECONDARY);

        newBillBtn.addActionListener(e -> {
            cart.clear();
            renderBill();
            cardLayout.show(mainCardsPanel, "BILLING");
        });

        backHomeBtn.addActionListener(e -> cardLayout.show(mainCardsPanel, "HOME"));

        actions.add(newBillBtn);
        actions.add(backHomeBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void buildReceiptText() {
        double subtotal = 0.0;
        for (CartItem item : cart.values()) subtotal += item.getAmount();

        double gst = subtotal * GST_RATE;
        double discount = (subtotal >= DISCOUNT_LIMIT) ? (subtotal * DISCOUNT_RATE) : 0.0;
        double grandTotal = subtotal + gst - discount;

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        sb.append("        SUPERMARKET BILLING SYSTEM\n");
        sb.append("==============================================\n");
        sb.append(String.format("Date    : %s\n", dateStr));
        sb.append(String.format("Cashier : %s\n", currentCashier));
        sb.append(String.format("Counter : %s\n", currentCounter));
        sb.append("----------------------------------------------\n");
        sb.append(String.format("%-21s %7s %5s %12s\n", "Item", "Price", "Qty", "Amount"));
        sb.append("----------------------------------------------\n");

        for (CartItem item : cart.values()) {
            String name = item.product.name;
            if (name.length() > 21) name = name.substring(0, 21);
            sb.append(String.format("%-21s %7s %5d %12s\n",
                    name,
                    MONEY_FMT.format(item.product.price),
                    item.quantity,
                    MONEY_FMT.format(item.getAmount())));
        }

        sb.append("----------------------------------------------\n");
        sb.append(String.format("Subtotal                         Rs. %s\n", MONEY_FMT.format(subtotal)));
        sb.append(String.format("GST (5%%)                         Rs. %s\n", MONEY_FMT.format(gst)));
        sb.append(String.format("Less: Discount                  Rs. %s\n", MONEY_FMT.format(discount)));
        sb.append("==============================================\n");
        sb.append(String.format("GRAND TOTAL                      Rs. %s\n", MONEY_FMT.format(grandTotal)));
        sb.append("==============================================\n");
        sb.append("        Thank you! Visit Again.\n");

        receiptTextArea.setText(sb.toString());
    }

    // ==========================================
    // 4. GOOGLE CALENDAR-INSPIRED PAGE
    // ==========================================
    private JPanel createCalendarPage() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(COLOR_PANEL_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Navigation
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(COLOR_PANEL_BG);

        monthYearLabel = new JLabel("", SwingConstants.LEFT);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 20));
        monthYearLabel.setForeground(COLOR_PRIMARY);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setBackground(COLOR_PANEL_BG);

        JButton prevBtn = createStyledButton("< Prev", COLOR_SECONDARY);
        JButton nextBtn = createStyledButton("Next >", COLOR_SECONDARY);
        JButton addEventBtn = createStyledButton("+ Add Event", COLOR_PRIMARY);
        JButton backBtn = createStyledButton("Back to Home", COLOR_SECONDARY);

        prevBtn.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendarDisplay();
        });

        nextBtn.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendarDisplay();
        });

        addEventBtn.addActionListener(e -> showAddEventDialog());
        backBtn.addActionListener(e -> cardLayout.show(mainCardsPanel, "HOME"));

        btnGroup.add(prevBtn);
        btnGroup.add(nextBtn);
        btnGroup.add(addEventBtn);
        btnGroup.add(backBtn);

        navPanel.add(monthYearLabel, BorderLayout.WEST);
        navPanel.add(btnGroup, BorderLayout.EAST);
        mainPanel.add(navPanel, BorderLayout.NORTH);

        // Center Split (Calendar Grid on Left, Day Agenda on Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setBackground(COLOR_PANEL_BG);

        // Left Grid Panel
        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(COLOR_PANEL_BG);

        JPanel weekHeaderGrid = new JPanel(new GridLayout(1, 7, 4, 4));
        weekHeaderGrid.setBackground(COLOR_PANEL_BG);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(COLOR_SECONDARY);
            weekHeaderGrid.add(lbl);
        }

        daysGridPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        daysGridPanel.setBackground(COLOR_PANEL_BG);

        calendarContainer.add(weekHeaderGrid, BorderLayout.NORTH);
        calendarContainer.add(daysGridPanel, BorderLayout.CENTER);

        // Right Agenda Panel
        JPanel agendaPanel = new JPanel(new BorderLayout(10, 10));
        agendaPanel.setBackground(COLOR_PANEL_BG);
        agendaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel agendaTitle = new JLabel("Agenda for Selected Date");
        agendaTitle.setFont(new Font("Arial", Font.BOLD, 14));
        agendaTitle.setForeground(COLOR_TEXT);

        dayAgendaModel = new DefaultListModel<>();
        JList<String> agendaList = new JList<>(dayAgendaModel);
        agendaList.setFont(new Font("Arial", Font.PLAIN, 12));

        agendaPanel.add(agendaTitle, BorderLayout.NORTH);
        agendaPanel.add(new JScrollPane(agendaList), BorderLayout.CENTER);

        splitPane.setLeftComponent(calendarContainer);
        splitPane.setRightComponent(agendaPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void updateCalendarDisplay() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        daysGridPanel.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekVal = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Empty Padding Cells
        for (int i = 0; i < dayOfWeekVal; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(248, 250, 252));
            daysGridPanel.add(emptyCell);
        }

        // Days Buttons
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("Arial", Font.PLAIN, 12));
            dayBtn.setFocusPainted(false);

            // Highlight events count
            long count = eventsList.stream().filter(e -> e.dateTime.toLocalDate().equals(date)).count();
            if (count > 0) {
                dayBtn.setText(day + " (" + count + ")");
                dayBtn.setForeground(COLOR_PRIMARY);
                dayBtn.setFont(new Font("Arial", Font.BOLD, 12));
            }

            if (date.equals(selectedDate)) {
                dayBtn.setBackground(new Color(209, 250, 229));
            } else {
                dayBtn.setBackground(Color.WHITE);
            }

            dayBtn.addActionListener(e -> {
                selectedDate = date;
                updateCalendarDisplay();
                updateAgendaList(date);
            });

            daysGridPanel.add(dayBtn);
        }

        // Fill Remaining Cells
        int totalCells = dayOfWeekVal + daysInMonth;
        int remaining = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < remaining; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(248, 250, 252));
            daysGridPanel.add(emptyCell);
        }

        daysGridPanel.revalidate();
        daysGridPanel.repaint();
        updateAgendaList(selectedDate);
    }

    private void updateAgendaList(LocalDate date) {
        dayAgendaModel.clear();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        List<CalendarEvent> filtered = eventsList.stream()
                .filter(e -> e.dateTime.toLocalDate().equals(date))
                .sorted(Comparator.comparing(e -> e.dateTime))
                .toList();

        if (filtered.isEmpty()) {
            dayAgendaModel.addElement("No scheduled tasks or reminders.");
        } else {
            for (CalendarEvent ev : filtered) {
                dayAgendaModel.addElement(String.format("[%s] %s - %s (%s)",
                        ev.dateTime.format(timeFmt), ev.type, ev.title, ev.description));
            }
        }
    }

    private void showAddEventDialog() {
        JDialog dialog = new JDialog(this, "Schedule New Event / Reminder", true);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<String> typeSelect = new JComboBox<>(new String[]{"Task", "Shift", "Sale", "Meeting", "Reminder"});

        String defaultDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JTextField dateField = new JTextField(defaultDate);
        JTextField timeField = new JTextField("10:00");

        dialog.add(new JLabel(" Event Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel(" Description:"));
        dialog.add(descField);
        dialog.add(new JLabel(" Category:"));
        dialog.add(typeSelect);
        dialog.add(new JLabel(" Date (YYYY-MM-DD):"));
        dialog.add(dateField);
        dialog.add(new JLabel(" Time (HH:MM):"));
        dialog.add(timeField);

        JButton saveBtn = createStyledButton("Save Event", COLOR_PRIMARY);
        saveBtn.addActionListener(e -> {
            try {
                LocalDate d = LocalDate.parse(dateField.getText().trim());
                String[] timeParts = timeField.getText().trim().split(":");
                LocalDateTime dt = d.atTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));

                eventsList.add(new CalendarEvent(
                        titleField.getText().trim(),
                        descField.getText().trim(),
                        dt,
                        (String) typeSelect.getSelectedItem()
                ));

                dialog.dispose();
                updateCalendarDisplay();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date or time format. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel(""));
        dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SupermarketSystem().setVisible(true);
        });
    }
}