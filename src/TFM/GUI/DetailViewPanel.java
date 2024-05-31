package TFM.GUI;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class DetailViewPanel extends JPanel implements TrafficPickedListener{

    private JLabel icaoCodeLabel;
    private JTextArea polygonDetailsArea;
    private JScrollPane polygonDetailsScrollPane;

    public DetailViewPanel() {
        initComponents();
    }

    private void initComponents() {
        icaoCodeLabel = new JLabel();
        polygonDetailsScrollPane = new JScrollPane();
        polygonDetailsArea = new JTextArea();

        icaoCodeLabel.setText("ICAO Code:");

        polygonDetailsArea.setColumns(20);
        polygonDetailsArea.setRows(5);
        polygonDetailsArea.setEditable(false);
        polygonDetailsScrollPane.setViewportView(polygonDetailsArea);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(icaoCodeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(polygonDetailsScrollPane, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(icaoCodeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(polygonDetailsScrollPane, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

    public void updateDetails(String icaoCode, String polygonDetails) {
        icaoCodeLabel.setText("ICAO Code: " + icaoCode);
        polygonDetailsArea.setText(polygonDetails);
    }

    public void clearDetails() {
        icaoCodeLabel.setText("");
        polygonDetailsArea.setText("");
    }
    
    @Override
    public void showDetails(String icaoCode, String polygonDetails) {
        updateDetails(icaoCode, polygonDetails);
        setVisible(true); // Ensure the panel is visible
    }

    @Override
    public void hideDetails() {
        clearDetails();
        setVisible(false); // Hide the panel
    }
}

