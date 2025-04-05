package electricity.billing.system;

import net.proteanit.sql.DbUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;

public class bill_details extends JFrame {
    String meter;
    JTable table;
    JButton payNowButton;
    double billAmount;

    bill_details(String meter) {
        this.meter = meter;
        setSize(700, 650);
        setLocation(400, 150);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        table = new JTable();

        try {
            database c = new database();
            String query_bill = "select * from bill where meter_no = '" +meter+ "'";
            ResultSet resultSet = c.statement.executeQuery(query_bill);
            table.setModel(DbUtils.resultSetToTableModel(resultSet));

            if (resultSet.next()) {
                billAmount = resultSet.getDouble("total_bill");
                String[] columnNames = {"Month", "Units", "Total Bill", "Status"};
                Object[][] data = {
                    {
                        resultSet.getString("month"),
                        resultSet.getString("units"),
                        resultSet.getString("total_bill"),
                        resultSet.getString("status")
                    }
                };
                table.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(0, 0, 700, 550);
        add(sp);

        payNowButton = new JButton("Pay Now");
        payNowButton.setBounds(250, 560, 200, 30);
        payNowButton.setBackground(new Color(32, 171, 214));
        payNowButton.setForeground(Color.WHITE);
        payNowButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        payNowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (billAmount > 0) {
                    new SecurePaymentGateway(meter, billAmount);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "No pending bills to pay!");
                }
            }
        });
        add(payNowButton);

        setVisible(true);
    }

    public static void main(String[] args) {
        new bill_details("");
    }
}
