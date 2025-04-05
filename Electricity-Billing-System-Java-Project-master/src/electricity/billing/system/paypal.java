package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.net.URI;

class PayBill extends JFrame implements ActionListener {
    Choice monthChoice;
    String meter;
    JButton payButton, backButton, qrButton;

    JLabel meterNumberLabel, nameLabel, unitLabel, totalBillLabel, statusLabel;
    JLabel meterNumberText, nameText, unitText, totalBillText, statusText;

    PayBill(String meter) {
        this.meter = meter;
        setSize(900, 600);
        setLocation(300, 150);
        setLayout(null);

        JLabel heading = new JLabel("Pay Bill");
        heading.setFont(new Font("Tahoma", Font.BOLD, 24));
        heading.setBounds(120, 5, 400, 30);
        add(heading);

        meterNumberLabel = new JLabel("Meter Number");
        meterNumberLabel.setBounds(35, 80, 200, 20);
        add(meterNumberLabel);

        meterNumberText = new JLabel(meter);
        meterNumberText.setBounds(300, 80, 200, 20);
        add(meterNumberText);

        nameLabel = new JLabel("Name");
        nameLabel.setBounds(35, 140, 200, 20);
        add(nameLabel);

        nameText = new JLabel();
        nameText.setBounds(300, 140, 200, 20);
        add(nameText);

        monthChoice = new Choice();
        for (String month : new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}) {
            monthChoice.add(month);
        }
        monthChoice.setBounds(300, 200, 150, 20);
        add(monthChoice);

        unitLabel = new JLabel("Unit");
        unitLabel.setBounds(35, 260, 200, 20);
        add(unitLabel);

        unitText = new JLabel();
        unitText.setBounds(300, 260, 200, 20);
        add(unitText);

        totalBillLabel = new JLabel("Total Bill");
        totalBillLabel.setBounds(35, 320, 200, 20);
        add(totalBillLabel);

        totalBillText = new JLabel();
        totalBillText.setBounds(300, 320, 200, 20);
        add(totalBillText);

        statusLabel = new JLabel("Status");
        statusLabel.setBounds(35, 380, 200, 20);
        add(statusLabel);

        statusText = new JLabel();
        statusText.setBounds(300, 380, 200, 20);
        statusText.setForeground(Color.RED);
        add(statusText);

        fetchCustomerData();
        monthChoice.addItemListener(e -> fetchBillData());

        payButton = new JButton("Pay via Razorpay");
        payButton.setBackground(Color.BLACK);
        payButton.setForeground(Color.WHITE);
        payButton.setBounds(100, 460, 150, 25);
        payButton.addActionListener(this);
        add(payButton);

        qrButton = new JButton("Pay by QR Code");
        qrButton.setBackground(Color.BLACK);
        qrButton.setForeground(Color.WHITE);
        qrButton.setBounds(270, 460, 150, 25);
        qrButton.addActionListener(this);
        add(qrButton);

        backButton = new JButton("Back");
        backButton.setBackground(Color.BLACK);
        backButton.setForeground(Color.WHITE);
        backButton.setBounds(440, 460, 100, 25);
        backButton.addActionListener(this);
        add(backButton);

        setVisible(true);
    }

    public void fetchCustomerData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/BIll_system1", "root", "Tapas@63")) {
            PreparedStatement stmt = con.prepareStatement("SELECT name FROM new_customer WHERE meterno = ?");
            stmt.setString(1, meter);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameText.setText(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchBillData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/BIll_system1", "root", "Tapas@63")) {
            PreparedStatement stmt = con.prepareStatement("SELECT unit, total_bill, status FROM bill WHERE meter_no = ? AND month = ?");
            stmt.setString(1, meter);
            stmt.setString(2, monthChoice.getSelectedItem());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                unitText.setText(rs.getString("unit"));
                totalBillText.setText(rs.getString("total_bill"));
                statusText.setText(rs.getString("status"));
            } else {
                unitText.setText("N/A");
                totalBillText.setText("N/A");
                statusText.setText("N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == payButton) {
            if ("Paid".equalsIgnoreCase(statusText.getText())) {
                JOptionPane.showMessageDialog(this, "Bill already paid!");
            } else {
                try {
                    String razorpayLink = "http://razorpay.me/@ELECTRICITYBILLS";
                    Desktop.getDesktop().browse(new URI(razorpayLink));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to open payment link. Error: " + ex.getMessage());
                }
            }
        } else if (e.getSource() == qrButton) {
            new PaymentQRCodeApp(meter, nameText.getText(), totalBillText.getText()).setVisible(true);
        } else if (e.getSource() == backButton) {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new PayBill("");
    }
}
