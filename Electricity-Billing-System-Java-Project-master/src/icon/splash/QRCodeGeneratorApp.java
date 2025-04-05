package electricity.billing.system;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGeneratorApp extends JFrame implements ActionListener {

    JTextField meterNoField, upiField;
    JButton fetchButton, generateButton;
    JLabel qrLabel;
    String name, amount;

    public QRCodeGeneratorApp() {
        setTitle("Payment QR Code Generator");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel meterLabel = new JLabel("Meter No:");
        meterLabel.setBounds(20, 20, 80, 25);
        add(meterLabel);

        meterNoField = new JTextField();
        meterNoField.setBounds(120, 20, 200, 25);
        add(meterNoField);

        fetchButton = new JButton("Fetch Details");
        fetchButton.setBounds(330, 20, 120, 25);
        fetchButton.addActionListener(this);
        add(fetchButton);

        JLabel upiLabel = new JLabel("UPI ID:");
        upiLabel.setBounds(20, 60, 80, 25);
        add(upiLabel);

        upiField = new JTextField();
        upiField.setBounds(120, 60, 200, 25);
        add(upiField);

        generateButton = new JButton("Generate QR Code");
        generateButton.setBounds(100, 100, 180, 30);
        generateButton.addActionListener(this);
        add(generateButton);

        qrLabel = new JLabel();
        qrLabel.setBounds(100, 150, 300, 300);
        add(qrLabel);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == fetchButton) {
                fetchData();
            } else if (e.getSource() == generateButton) {
                generateQRCodeAction();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void fetchData() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/BIll_system1", "root", "Tapas@63")) {
            String meterNo = meterNoField.getText();
            if (meterNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Meter Number.");
                return;
            }

            String query = "SELECT name, total_bill FROM bill WHERE meter_no = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, meterNo);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    name = rs.getString("name");
                    amount = rs.getString("total_bill");
                    JOptionPane.showMessageDialog(this, "Details Fetched Successfully: Name - " + name + ", Amount - ₹" + amount);
                } else {
                    JOptionPane.showMessageDialog(this, "No record found for Meter Number: " + meterNo);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void generateQRCodeAction() throws Exception {
        String upiID = upiField.getText();

        if (upiID.isEmpty() || name == null || amount == null) {
            JOptionPane.showMessageDialog(this, "Please fetch details and enter UPI ID.");
            return;
        }

        String upiUrl = String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=INR", upiID, name, amount);
        String filePath = "payment_qr.png";
        generateQRCode(upiUrl, filePath);

        ImageIcon qrImage = new ImageIcon(ImageIO.read(new File(filePath)));
        qrLabel.setIcon(new ImageIcon(qrImage.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
        JOptionPane.showMessageDialog(this, "QR Code generated successfully!");
    }

    public void generateQRCode(String data, String filePath) throws Exception {
        int width = 300;
        int height = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        Path path = new File(filePath).toPath();
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        System.out.println("QR Code saved as " + filePath);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QRCodeGeneratorApp().setVisible(true));
    }
}
