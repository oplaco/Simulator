/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package TFM.GUI;

import TFM.Core.Simulation;
import TFM.simulationEvents.SimulationEvent;
import TFM.simulationEvents.SimulationFileReader;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Gabriel
 */
public class Console extends javax.swing.JPanel implements ExecuteCommandListener  {
    
    //Simulation to which the time is going to be managed
    private Simulation simulation;

    /**
     * Creates new form TimeManagementMenu
     */
    public Console(Simulation simulation) {
        this.simulation = simulation;
        this.simulation.setExecuteCommandListener(this);
        
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        consoleTextField = new javax.swing.JTextField();

        consoleTextArea.setColumns(20);
        consoleTextArea.setRows(5);
        consoleTextArea.setEnabled(false);
        jScrollPane1.setViewportView(consoleTextArea);

        consoleTextField.setToolTipText("");
        consoleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consoleTextFieldActionPerformed(evt);
            }
        });
        consoleTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consoleTextFieldKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addComponent(consoleTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(consoleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void consoleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consoleTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_consoleTextFieldActionPerformed

    private void consoleTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_consoleTextFieldKeyPressed
        // The Enter key was pressed, add your handling code here
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            SimulationEvent event = SimulationFileReader.createEventFromString(this.simulation.getSimulationTime(),consoleTextField.getText());

            if (event == null){
                this.appendToConsole("Invalid input","error");
            } else {
                //this.appendToConsole(consoleTextField.getText(),"normal");
                this.consoleTextField.setText("");
                this.simulation.getEvents().add(event);
                //add the event to the event list
            }
            
        }
    }//GEN-LAST:event_consoleTextFieldKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JTextField consoleTextField;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

public void appendToConsole(String text, String status) {
    // Create a color variable based on the status
    Color textColor;
    switch (status.toLowerCase()) {
        case "error":
            textColor = Color.RED;
            break;
        case "warning":
            textColor = Color.YELLOW;
            break;
        default:
            textColor = Color.BLACK;
    }

    // Append the text to the TextArea with the specified color
    consoleTextArea.setForeground(textColor);
    consoleTextArea.append(text + "\n");

    // Optionally, you can auto-scroll to the bottom
    consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
}

    @Override
    public void onCommandExecuted(String message, String status) {
        this.appendToConsole(message, status);
    }

}
