package electricity.billing.system;

import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class BillReminder extends JFrame {

    public static final String ACCOUNT_SID = System.getenv("AC2926e008e9297f7ed62c63b3f0db0f26");
    public static final String AUTH_TOKEN = System.getenv("eaea0531637adea809f271ea43430dac");
    public static final String TWILIO_PHONE_NUMBER = System.getenv("+14432419460");

    JTable table;
    JButton sendReminder, close;
    JLabel messageLabel;

    BillReminder() {
        super("Bill Reminder");
        getContentPane().setBackground(new Color(192, 186, 254));
        setSize(700, 500);
        setLocation(400, 200);
        setLayout(null);

        JLabel heading = new JLabel("Unpaid Bills - Reminder System");
        heading.setBounds(20, 20, 300, 20);
        heading.setFont(new Font("Tahoma", Font.BOLD, 16));
        add(heading);

        table = new JTable();
        loadUnpaidData();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 60, 650, 300);
        add(scrollPane);

        sendReminder = new JButton("Send Reminder");
        sendReminder.setBackground(Color.white);
        sendReminder.setBounds(200, 380, 150, 30);
        sendReminder.addActionListener(e -> sendReminderAction());
        add(sendReminder);

        close = new JButton("Close");
        close.setBackground(Color.white);
        close.setBounds(400, 380, 100, 30);
        close.addActionListener(e -> setVisible(false));
        add(close);

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setBounds(20, 420, 650, 30);
        messageLabel.setForeground(Color.RED);
        add(messageLabel);

        setVisible(true);
    }

    private void loadUnpaidData() {
        try {
            database c = new database();
            String query = "SELECT nc.meterno, b.status FROM bill b JOIN new_customer nc ON b.meter_no = nc.meterno WHERE b.status = 'Not paid'";
            System.out.println("Executing query: " + query);
            ResultSet resultSet = c.statement.executeQuery(query);

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No unpaid bills found.");
            } else {
                table.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendReminderAction() {
        if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_PHONE_NUMBER == null) {
            System.out.println("Please upgrade your TWILIO account. Please set the TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER environment variables.");
            messageLabel.setText("Twilio account configuration error.");
            return;
        }

        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("SELECT nc.meterno, nc.phone_no FROM bill b JOIN new_customer nc ON b.meter_no = nc.meterno WHERE b.status = 'Not paid'");

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No unpaid bills to send reminders.");
                messageLabel.setText("No unpaid bills to send reminders.");
                return;
            }

            while (resultSet.next()) {
                String meterNo = resultSet.getString("meterno");
                String phone = resultSet.getString("phone_no");

                if (phone != null && !phone.isEmpty() && phone.matches("\\+\\d{10,15}")) {
                    try {
                        Message message = Message.creator(
                                new com.twilio.type.PhoneNumber(phone),
                                new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                                "Reminder: Your electricity bill is unpaid. Please clear your dues to avoid disconnection."
                        ).create();
                        System.out.println("SMS sent to Meter No: " + meterNo + " (Phone: " + phone + ")");
                    } catch (Exception ex) {
                        System.out.println("Failed to send SMS to Meter No: " + meterNo + " - " + ex.getMessage());
                    }
                } else {
                    System.out.println("Invalid phone number for Meter No: " + meterNo);
                }
            }
            messageLabel.setText("Reminders Sent Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while sending reminders.");
        }
    }

    public static void main(String[] args) {
        new BillReminder();
    }
}
