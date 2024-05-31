package TFM.GUI;

import TFM.utils.UnitConversion;
import classes.base.TrafficSimulated;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class DetailViewPanel extends JPanel implements TrafficPickedListener {

    private JLabel icaoCodeLabel;
    private JLabel altitudeLabel;
    private JLabel speedLabel;
    private JLabel bearingLabel;
    private DecimalFormat decimalFormat;

    public DetailViewPanel() {
        initComponents();
    }

    private void initComponents() {
        // Initialize labels with larger font
        icaoCodeLabel = new JLabel("ICAO Code:");
        altitudeLabel = new JLabel("Altitude:");
        speedLabel = new JLabel("Speed:");
        bearingLabel = new JLabel("Bearing:");

        Font labelFont = icaoCodeLabel.getFont().deriveFont(16.0f); // Increase font size
        icaoCodeLabel.setFont(labelFont);
        altitudeLabel.setFont(labelFont);
        speedLabel.setFont(labelFont);
        bearingLabel.setFont(labelFont);

        // Set layout to BoxLayout with Y_AXIS orientation
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add labels to the panel with vertical struts for spacing
        add(icaoCodeLabel);
        add(Box.createVerticalStrut(10)); // Add space between labels
        add(altitudeLabel);
        add(Box.createVerticalStrut(10)); // Add space between labels
        add(speedLabel);
        add(Box.createVerticalStrut(10)); // Add space between labels
        add(bearingLabel);

        // Optional: add some padding around the panel
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize DecimalFormat for two decimal places
        decimalFormat = new DecimalFormat("#.##");
    }

    public void updateDetails(TrafficSimulated traffic) {
        icaoCodeLabel.setText("ICAO Code: " + traffic.getHexCode());
        altitudeLabel.setText("Altitude: " + decimalFormat.format(traffic.getPosition().getAltitude()/UnitConversion.ftToMeter) + " ft");
        speedLabel.setText("Speed: " + decimalFormat.format(traffic.getSpeed()) + " kts");
        bearingLabel.setText("Bearing: " + decimalFormat.format(traffic.getCourse()) + " º");
    }

    public void clearDetails() {
        icaoCodeLabel.setText("ICAO Code:");
        altitudeLabel.setText("Altitude:");
        speedLabel.setText("Speed:");
        bearingLabel.setText("Bearing:");
    }

    @Override
    public void showDetails(TrafficSimulated traffic) {
        updateDetails(traffic);
        slide(true, 15); 
    }

    @Override
    public void hideDetails() {
        slide(false, 15); 
    }

    private void slide(boolean slideIn, int slideStep) {
        setVisible(true); // Ensure the panel is visible during sliding
        Timer timer = new Timer(10, null);
        timer.addActionListener(e -> {
            int currentX = getX();
            int targetX = slideIn ? getParent().getWidth() - getWidth() : getParent().getWidth();
            if ((slideIn && currentX > targetX) || (!slideIn && currentX < targetX)) {
                setLocation(currentX - (slideIn ? slideStep : -slideStep), getY());
                revalidate();
                repaint();
            } else {
                if (!slideIn) {
                    setVisible(false); // Hide the panel when sliding out is complete
                }
                timer.stop();
            }
        });
        timer.start();
    }
}
