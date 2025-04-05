package electricity.billing.system;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PaymentQRCodeApp extends JFrame {

    JLabel qrLabel;
    String meterNo, name, amount;

    public PaymentQRCodeApp(String meterNo, String name, String amount) {
        this.meterNo = meterNo;
        this.name = name;
        this.amount = amount;

        setTitle("Payment QR Code Generator");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel headingLabel = new JLabel("Scan QR Code to Pay");
        headingLabel.setBounds(150, 20, 200, 25);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(headingLabel);

        qrLabel = new JLabel();
        qrLabel.setBounds(100, 80, 300, 300);
        add(qrLabel);

        JButton backButton = new JButton("Back");
        backButton.setBounds(200, 400, 100, 30);
        backButton.addActionListener(e -> {
            new PayBill(""); // Assuming PayBill is the previous page class
            dispose();
        });
        add(backButton);

        generateQRCodeAction();

        setVisible(true);
    }

    private void generateQRCodeAction() {
        try {
            String upiID = "8455961688@axl";
            String note = "Electricity Bill Payment";

            String upiUrl = String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=%s", upiID, name, amount, note);
            String filePath = "payment_qr.png";
            generateQRCode(upiUrl, filePath);

            ImageIcon qrImage = new ImageIcon(ImageIO.read(new File(filePath)));
            qrLabel.setIcon(new ImageIcon(qrImage.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
            JOptionPane.showMessageDialog(this, "QR Code generated successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating QR Code: " + ex.getMessage());
        }
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
        new PaymentQRCodeApp("12345", "John Doe", "500");
    }
}
