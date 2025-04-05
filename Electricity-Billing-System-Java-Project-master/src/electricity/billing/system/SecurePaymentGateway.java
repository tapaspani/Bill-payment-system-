package electricity.billing.system;

import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import javax.swing.*;

public class SecurePaymentGateway extends JFrame implements ActionListener {
    private JTabbedPane paymentTabs;
    private JPanel cardPanel, upiPanel;
    private JButton payCardButton, payUPIButton, backButton;
    private JTextField cardNumber, nameOnCard, upiId;
    private JPasswordField cvv;
    private JComboBox<String> expiryMonth, expiryYear, cardType;
    private String meterNumber;
    private double billAmount;
    private static final String SALT = "ElectricityBill2024";

    public SecurePaymentGateway(String meterNo, double amount) {
        this.meterNumber = meterNo;
        this.billAmount = amount;
        
        setLayout(null);
        setBounds(300, 150, 800, 600);
        getContentPane().setBackground(Color.WHITE);
        setTitle("Secure Payment Gateway");

        paymentTabs = new JTabbedPane();
        paymentTabs.setBounds(20, 20, 740, 480);
        

        initializeCardPanel();
        

        initializeUPIPanel();
        

        paymentTabs.addTab("Card Payment", cardPanel);
        paymentTabs.addTab("UPI Payment", upiPanel);
        add(paymentTabs);
        

        backButton = new JButton("Back");
        backButton.setBounds(350, 510, 100, 30);
        backButton.addActionListener(this);
        add(backButton);
        
        setVisible(true);
    }
    
    private void initializeCardPanel() {
        cardPanel = new JPanel(null);
        cardPanel.setBackground(Color.WHITE);
        

        JLabel typeLabel = new JLabel("Card Type");
        typeLabel.setBounds(50, 30, 100, 25);
        cardPanel.add(typeLabel);
        
        String[] types = {"Credit Card", "Debit Card"};
        cardType = new JComboBox<>(types);
        cardType.setBounds(200, 30, 200, 25);
        cardPanel.add(cardType);
        

        JLabel numberLabel = new JLabel("Card Number");
        numberLabel.setBounds(50, 80, 100, 25);
        cardPanel.add(numberLabel);
        
        cardNumber = new JTextField();
        cardNumber.setBounds(200, 80, 200, 25);
        cardPanel.add(cardNumber);
        

        JLabel nameLabel = new JLabel("Name on Card");
        nameLabel.setBounds(50, 130, 100, 25);
        cardPanel.add(nameLabel);
        
        nameOnCard = new JTextField();
        nameOnCard.setBounds(200, 130, 200, 25);
        cardPanel.add(nameOnCard);
        

        JLabel expiryLabel = new JLabel("Valid Through");
        expiryLabel.setBounds(50, 180, 100, 25);
        cardPanel.add(expiryLabel);
        
        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        expiryMonth = new JComboBox<>(months);
        expiryMonth.setBounds(200, 180, 60, 25);
        cardPanel.add(expiryMonth);
        
        String[] years = new String[10];
        int currentYear = LocalDateTime.now().getYear() % 100;
        for(int i = 0; i < 10; i++) {
            years[i] = String.format("%02d", currentYear + i);
        }
        expiryYear = new JComboBox<>(years);
        expiryYear.setBounds(270, 180, 60, 25);
        cardPanel.add(expiryYear);
        

        JLabel cvvLabel = new JLabel("CVV");
        cvvLabel.setBounds(50, 230, 100, 25);
        cardPanel.add(cvvLabel);
        
        cvv = new JPasswordField();
        cvv.setBounds(200, 230, 60, 25);
        cardPanel.add(cvv);
        

        JLabel amountLabel = new JLabel("Amount to Pay: ₹" + String.format("%.2f", billAmount));
        amountLabel.setBounds(50, 280, 300, 25);
        amountLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        cardPanel.add(amountLabel);
        

        payCardButton = new JButton("Pay Now");
        payCardButton.setBounds(200, 330, 150, 35);
        payCardButton.setBackground(new Color(0, 128, 0));
        payCardButton.setForeground(Color.WHITE);
        payCardButton.addActionListener(this);
        cardPanel.add(payCardButton);
    }
    
    private void initializeUPIPanel() {
        upiPanel = new JPanel(null);
        upiPanel.setBackground(Color.WHITE);
        
        JLabel upiLabel = new JLabel("Enter UPI ID");
        upiLabel.setBounds(50, 50, 100, 25);
        upiPanel.add(upiLabel);
        
        upiId = new JTextField();
        upiId.setBounds(200, 50, 200, 25);
        upiPanel.add(upiId);
        
        JLabel amountLabel = new JLabel("Amount to Pay: ₹" + String.format("%.2f", billAmount));
        amountLabel.setBounds(50, 100, 300, 25);
        amountLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        upiPanel.add(amountLabel);
        
        payUPIButton = new JButton("Pay with UPI");
        payUPIButton.setBounds(200, 150, 150, 35);
        payUPIButton.setBackground(new Color(0, 128, 0));
        payUPIButton.setForeground(Color.WHITE);
        payUPIButton.addActionListener(this);
        upiPanel.add(payUPIButton);
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == payCardButton) {
            if (validateCardDetails()) {
                processCardPayment();
            }
        } else if (ae.getSource() == payUPIButton) {
            if (validateUPIDetails()) {
                processUPIPayment();
            }
        } else if (ae.getSource() == backButton) {
            setVisible(false);
            new bill_details(meterNumber);
        }
    }
    
    private boolean validateCardDetails() {
        String cardNum = cardNumber.getText().replaceAll("\\s", "");
        if (!cardNum.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(this, "Invalid card number! Must be 16 digits.");
            return false;
        }
        
        if (nameOnCard.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter name on card!");
            return false;
        }
        
        String cvvText = new String(cvv.getPassword());
        if (!cvvText.matches("\\d{3}")) {
            JOptionPane.showMessageDialog(this, "Invalid CVV! Must be 3 digits.");
            return false;
        }
        
        return true;
    }
    
    private boolean validateUPIDetails() {
        String upi = upiId.getText().trim();
        if (!upi.matches("[a-zA-Z0-9._-]+@[a-zA-Z]+")) {
            JOptionPane.showMessageDialog(this, "Invalid UPI ID!");
            return false;
        }
        return true;
    }
    
    private void processCardPayment() {
        try {
            // Generate transaction ID
            String transactionId = UUID.randomUUID().toString();
            
            // Encrypt sensitive data
            String encryptedCardNumber = encryptData(cardNumber.getText());
            
            // Store payment details
            database c = new database();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String query = "INSERT INTO transactions (transaction_id, meter_no, amount, payment_date, " +
                          "payment_method, status, card_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = c.connection.prepareStatement(query);
            pstmt.setString(1, transactionId);
            pstmt.setString(2, meterNumber);
            pstmt.setDouble(3, billAmount);
            pstmt.setString(4, timestamp);
            pstmt.setString(5, "CARD");
            pstmt.setString(6, "SUCCESS");
            pstmt.setString(7, cardType.getSelectedItem().toString());
            
            pstmt.executeUpdate();
            
            // Update bill status
            String updateQuery = "UPDATE bill SET status='Paid', payment_date=? WHERE meter_no=? AND status='Unpaid'";
            pstmt = c.connection.prepareStatement(updateQuery);
            pstmt.setString(1, timestamp);
            pstmt.setString(2, meterNumber);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Payment Successful!\nTransaction ID: " + transactionId);
            generateReceipt(transactionId);
            
            setVisible(false);
            new bill_details(meterNumber);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment Failed! Please try again.");
        }
    }
    
    private void processUPIPayment() {
        try {
            String transactionId = UUID.randomUUID().toString();
            database c = new database();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String query = "INSERT INTO transactions (transaction_id, meter_no, amount, payment_date, " +
                          "payment_method, status, upi_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = c.connection.prepareStatement(query);
            pstmt.setString(1, transactionId);
            pstmt.setString(2, meterNumber);
            pstmt.setDouble(3, billAmount);
            pstmt.setString(4, timestamp);
            pstmt.setString(5, "UPI");
            pstmt.setString(6, "SUCCESS");
            pstmt.setString(7, upiId.getText());
            
            pstmt.executeUpdate();
            
            String updateQuery = "UPDATE bill SET status='Paid', payment_date=? WHERE meter_no=? AND status='Unpaid'";
            pstmt = c.connection.prepareStatement(updateQuery);
            pstmt.setString(1, timestamp);
            pstmt.setString(2, meterNumber);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Payment Successful!\nTransaction ID: " + transactionId);
            generateReceipt(transactionId);
            
            setVisible(false);
            new bill_details(meterNumber);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment Failed! Please try again.");
        }
    }
    
    private String encryptData(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest((data + SALT).getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private void generateReceipt(String transactionId) {
        // Implementation for receipt generation
        // This can be extended to generate PDF receipts
    }
} 