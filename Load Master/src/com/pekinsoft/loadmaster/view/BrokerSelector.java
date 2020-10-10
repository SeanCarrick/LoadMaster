/*
 * Copyright (C) 2020 PekinSOFT Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * *****************************************************************************
 * *****************************************************************************
 *  Project    :   Load_Master
 *  Class      :   BrokerSelector.java
 *  Author     :   Sean Carrick
 *  Created    :   Aug 30, 2020 @ 9:00:42 AM
 *  Modified   :   Aug 30, 2020
 *  
 *  Purpose:
 *      Provides a method of visually selecting the broker/agent who booked the
 *      load, so that their contact information will be available on the load
 *      window and from the other load task windows.
 *  
 *  Revision History:
 *  
 *  WHEN          BY                  REASON
 *  ------------  ------------------- ------------------------------------------
 *  Aug 30, 2020  Sean Carrick        Initial creation.
 *  Sep 01, 2020  Sean Carrick        Added data validation to the dialog to 
 *                                    ensure that required fields are completed
 *                                    and that the date entered is valid.
 *  Oct 09, 2020  Sean Carrick        Removed the main() method from the class.
 * *****************************************************************************
 */
package com.pekinsoft.loadmaster.view;

import com.pekinsoft.loadmaster.Starter;
import com.pekinsoft.loadmaster.controller.BrokerCtl;
import com.pekinsoft.loadmaster.err.DataStoreException;
import com.pekinsoft.loadmaster.model.BrokerModel;
import com.pekinsoft.loadmaster.utils.MessageBox;
import com.pekinsoft.loadmaster.utils.ScreenUtils;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Sean Carrick
 */
public class BrokerSelector extends javax.swing.JDialog {
    
    private BrokerModel broker;
    private BrokerCtl records;
    private LogRecord lr = new LogRecord(Level.ALL, "Logging initialized for "
            + "BrokerSelector.");
    
    /**
     * Creates new form BrokerSelector
     */
    public BrokerSelector(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        
        Starter.logger.config(lr);
        lr = new LogRecord(Level.ALL, "Logging initialized for BrokerSelector.");
        lr.setSourceClassName(Brokers.class.getName());
        lr.setSourceMethodName("BrokerSelector");
        Starter.logger.enter(lr);
        
        lr.setMessage("Attempting to access the brokers database...");
        Starter.logger.debug(lr);
        try {
            records = new BrokerCtl();
            lr.setMessage("Brokers database accessed successfully!");
            Starter.logger.info(lr);
        } catch ( DataStoreException ex ) {
            lr.setMessage("Something went wrong accessing the brokers database.");
            lr.setThrown(ex);
            Starter.logger.error(lr);
            
            MessageBox.showError(ex, "Database Access");
            
            records = null;
        }
        
        initComponents();
        
        loadBrokerList();
        
        setLocation(ScreenUtils.centerDialog(this));
    }
    
    private void loadBrokerList() {
        brokerList.removeAllItems();
        brokerList.addItem("Select Broker/Agent...");
        
        try {
            records.first();
        } catch ( DataStoreException ex ) {
            lr.setMessage("Something went wrong moving to the next record.");
            lr.setThrown(ex);
            Starter.logger.error(lr);

            MessageBox.showError(ex, "Database Access");
        }
        
        for ( int x = 0; x < records.getRecordCount(); x++ ) {
            BrokerModel b = records.get();
            
            brokerList.addItem(b.getContact() + " (" + b.getId() + ")");
            
            try {
                if (records.hasNext() ) 
                    records.next();
            } catch ( DataStoreException ex ) {
                lr.setMessage("Something went wrong moving to the next record.");
                lr.setThrown(ex);
                Starter.logger.error(lr);

//                MessageBox.showError(ex, "Database Access");
            }
        }
    }
    
    public BrokerModel getSelectedBroker() {
        return broker;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        brokerList = new javax.swing.JComboBox<>();
        selectBroker = new javax.swing.JButton();

        setTitle("Select Load Broker/Agent");
        setAlwaysOnTop(true);
        setModal(true);
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setResizable(false);

        jLabel1.setText("Broker/Agent:");

        brokerList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                brokerListItemStateChanged(evt);
            }
        });

        selectBroker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/pekinsoft/loadmaster/res/ok.png"))); // NOI18N
        selectBroker.setMnemonic('S');
        selectBroker.setText("Select Broker/Agent");
        selectBroker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectBrokerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(brokerList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 232, Short.MAX_VALUE)
                        .addComponent(selectBroker)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(brokerList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectBroker)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectBrokerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectBrokerActionPerformed
        // In order to get the selected broker, we need to loop through the
        //+ records to find which record has the selected ID number.
        if ( !brokerList.getSelectedItem().toString().equalsIgnoreCase(
                "select broker/agent...") ) {
            String selectedBroker = brokerList.getSelectedItem().toString();
            long brokerID = Long.valueOf(selectedBroker.substring(
                    selectedBroker.indexOf("(") + 1,    // Start after (
                    selectedBroker.indexOf(")")));  // End before )
        
            try {
                records.first();

                for ( int x = 0; x < records.getRecordCount(); x++ ) {
                    BrokerModel b = records.get();

                    if ( brokerID == b.getId() ) {
                        broker = b;
                        break;
                    } else {
                        if ( records.hasNext() ) 
                            records.next();
                    }
                }
        
                setVisible(false); 
            } catch ( DataStoreException ex ) {
                lr.setMessage("Something went wrong moving to the next record.");
                lr.setThrown(ex);
                Starter.logger.error(lr);

    //            MessageBox.showError(ex, "Database Access");

                dispose();
            }
        }       
    }//GEN-LAST:event_selectBrokerActionPerformed

    private void brokerListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_brokerListItemStateChanged
        selectBroker.setEnabled(!brokerList.getSelectedItem().toString().equals("Select Broker/Agent..."));
    }//GEN-LAST:event_brokerListItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> brokerList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton selectBroker;
    // End of variables declaration//GEN-END:variables
}
