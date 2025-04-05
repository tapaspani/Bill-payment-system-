package electricity.billing.system;

import net.proteanit.sql.DbUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class deposit_details extends JFrame implements ActionListener {
    Choice searchMeterCho, searchMonthCho;
    JTable table;
    JButton search, print, close, reminder;

    deposit_details() {
        super("Deposit Details");
        getContentPane().setBackground(new Color(192, 186, 254));
        setSize(700, 500);
        setLocation(400, 200);
        setLayout(null);

        JLabel searchMeter = new JLabel("Search By Meter Number");
        searchMeter.setBounds(20, 20, 150, 20);
        add(searchMeter);

        searchMeterCho = new Choice();
        searchMeterCho.setBounds(180, 20, 150, 20);
        add(searchMeterCho);

        try {
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from bill");
            while (resultSet.next()) {
                searchMeterCho.add(resultSet.getString("meter_no"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel searchMonth = new JLabel("Search By Month");
        searchMonth.setBounds(400, 20, 100, 20);
        add(searchMonth);

        searchMonthCho = new Choice();
        searchMonthCho.add("January");
        searchMonthCho.add("February");
        searchMonthCho.add("March");
        searchMonthCho.add("April");
        searchMonthCho.add("May");
        searchMonthCho.add("June");
        searchMonthCho.add("July");
        searchMonthCho.add("August");
        searchMonthCho.add("September");
        searchMonthCho.add("October");
        searchMonthCho.add("November");
        searchMonthCho.add("December");
        searchMonthCho.setBounds(520, 20, 150, 20);
        add(searchMonthCho);

        table = new JTable();
        try {
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from bill");
            table.setModel(DbUtils.resultSetToTableModel(resultSet));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(0, 100, 700, 350);
        scrollPane.setBackground(Color.white);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        search = new JButton("Search");
        search.setBackground(Color.white);
        search.setBounds(20, 70, 80, 20);
        search.addActionListener(this);
        add(search);

        print = new JButton("Print");
        print.setBackground(Color.white);
        print.setBounds(120, 70, 80, 20);
        print.addActionListener(this);
        add(print);

        reminder = new JButton("Reminder");
        reminder.setBackground(Color.white);
        reminder.setBounds(220, 70, 100, 20);
        reminder.addActionListener(this);
        add(reminder);

        close = new JButton("Close");
        close.setBackground(Color.white);
        close.setBounds(600, 70, 80, 20);
        close.addActionListener(this);
        add(close);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == search) {
            String meter = searchMeterCho.getSelectedItem();
            String month = searchMonthCho.getSelectedItem();

            String query_search = "SELECT * FROM bill WHERE meter_no = '" + meter + "' AND month = '" + month + "'";
            try {
                database c = new database();
                ResultSet resultSet = c.statement.executeQuery(query_search);
                table.setModel(DbUtils.resultSetToTableModel(resultSet));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == print) {
            try {
                table.print();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == reminder) {
            new BillReminder();
        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new deposit_details();
    }
}
