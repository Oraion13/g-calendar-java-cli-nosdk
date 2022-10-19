package authentication;

import javax.swing.*;

public class ImageJFrame {
    ImageJFrame() {
        JFrame f = new JFrame("Scan This QR To Get Secret Key");
        ImageIcon icon = new ImageIcon("QRCode.png");
        f.add(new JLabel(icon));
        f.pack();
        f.setVisible(true);
    }
}