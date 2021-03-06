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
 *  ******************************************************************************
 *   Project:  Load_Master
 *   Module:   FuelPurchaseDialog
 *   Created:  Oct 20, 2020
 *   Modified: Oct 20, 2020
 * 
 *   Purpose:
 * 
 *   Revision History
 * 
 *   WHEN          BY                  REASON
 *   ------------  ------------------- ------------------------------------------
 *   Oct 20, 2020    Sean Carrick Initial Creation.
 *  ******************************************************************************
 */
package com.pekinsoft.loadmaster.view;

import com.pekinsoft.loadmaster.Starter;
import com.pekinsoft.loadmaster.controller.EntryCtl;
import com.pekinsoft.loadmaster.controller.FuelPurchaseCtl;
import com.pekinsoft.loadmaster.err.DataStoreException;
import com.pekinsoft.loadmaster.model.EntryModel;
import com.pekinsoft.loadmaster.model.FuelPurchaseModel;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author Sean Carrick &lt;sean at pekinsoft dot com&gt;
 */
public class FuelPurchaseDialog extends javax.swing.JDialog {
    private LogRecord entry;

    /**
     * Creates new form FuelPurchaseDialog
     */
    public FuelPurchaseDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        entry = new LogRecord(Level.ALL, "Initializing the FuelPurchaseDialog");
        entry.setSourceClassName(getClass().getName());
        entry.setSourceMethodName("FuelPurchaseDialog (Constructor)");
        entry.setParameters(new Object[]{parent, modal});
        Starter.logger.enter(entry);
        
        initComponents();
        
        getRootPane().setDefaultButton(okButton);
        
        int left = (Toolkit.getDefaultToolkit().getScreenSize().width 
                - getWidth() ) / 2;
        int top = (Toolkit.getDefaultToolkit().getScreenSize().height 
                - getHeight() ) / 2;
        setLocation(left, top);
        
        defPanel.setVisible(defCheckBox.isSelected());
    }
    
    private void doClose() {
        dispose();
    }
    
    private void doSave() {
        // First thing to do is to store the fuel purchase to the journal.
        FuelPurchaseModel purchase = new FuelPurchaseModel();
        purchase.setDate(datePicker.getDate());
        purchase.setDefPurchased(defCheckBox.isSelected());
        try {
            NumberFormat nf = new DecimalFormat("#,##0.000");
            Number amt = nf.parse(dieselGallonsField.getText());
            purchase.setGallonsOfDiesel(amt.doubleValue());
            amt = nf.parse(dieselPriceField.getText());
            purchase.setPricePerGallonDiesel(amt.doubleValue());
        } catch ( ParseException ex ) {
            entry.setSourceMethodName("doSave");
            entry.setMessage(ex.getMessage());
            entry.setParameters(null);
            entry.setThrown(ex);
            Starter.logger.error(entry);
        }
        purchase.setNotes(notesField.getText());
        purchase.setId(Long.parseLong(idField.getText()));
        
        // Now, store the DEF information, if DEF was purchased.
        if ( defCheckBox.isSelected() ) {
            try {
                NumberFormat nf = new DecimalFormat("#,##0.000");
                Number amt = nf.parse(defGallonsField.getText());
                purchase.setGallonsOfDef(amt.doubleValue());
                amt = nf.parse(defPriceField.getText());
                purchase.setPricePerGallonDef(amt.doubleValue());
            } catch ( ParseException ex ) {
                entry.setSourceMethodName("doSave");
                entry.setMessage(ex.getMessage());
                entry.setParameters(null);
                entry.setThrown(ex);
                Starter.logger.error(entry);
            }
        } else {
            purchase.setGallonsOfDef(0.0);
            purchase.setPricePerGallonDef(0.0);
        }
        
        // Now that we have created our FuelPurchaseModel object and provided it
        //+ its data, we can add it to the Fuel Account Journal.
        FuelPurchaseModel model = new FuelPurchaseModel();
        model.setDate(datePicker.getDate());
        model.setDefPurchased(defCheckBox.isSelected());
        model.setGallonsOfDef(defGallonsField.getText());
        model.setPricePerGallonDef(defPriceField.getText());
        model.setGallonsOfDiesel(dieselGallonsField.getText());
        model.setPricePerGallonOfDiesel(dieselPriceField.getText());
        model.setId(idField.getText());
        model.setLocation(locationField.getText());
        model.setNotes(notesField.getText());
        model.setOdometer(odometerField.getText());
        
        try {
            FuelPurchaseCtl records = new FuelPurchaseCtl();
            
            records.addNew(model);
            records.close();
            
        } catch ( DataStoreException ex ) {
            
        }
        
        // Since the batch EntryCtl object is not null, the user is using 
        //+ the batch posting method, so we need to add this transaction to
        //+ the batch of transactions.
        EntryModel transaction = new EntryModel();
        transaction.setAmount(purchase.getGallonsOfDiesel() 
                * purchase.getPricePerGallonDiesel());
        transaction.setBalanced(false);
        transaction.setCode("Diesel");
        transaction.setDate(purchase.getDate());
        transaction.setDeductible(true);
        transaction.setDescription(purchase.getGallonsOfDiesel() 
                + " gallons at " + purchase.getLocation());
        transaction.setFromAccount(50040);
        transaction.setToAccount(10040);

        // If the user is using the batch posting method, we need to add this
        //+ purchase to the batch.
        if ( LoadMaster.batch != null ) 
            LoadMaster.batch.addNew(transaction);
        else {
            // We need to get our general ledger and add this transaction to it.
            try {
                // Get our General Ledger.
                EntryCtl ledger = new EntryCtl();
                
                // Add our new transaction to it.
                ledger.addNew(transaction);
                
                // Close the General Ledger, thereby writing the data out to it.
                ledger.close();
            } catch ( DataStoreException ex ) {
                entry.setSourceMethodName("doSave");
                entry.setMessage(ex.getMessage());
                entry.setParameters(null);
                entry.setThrown(ex);
                Starter.logger.error(entry);
            }
        }

        // Next, we need to check to see if DEF was also purchased.
        if ( purchase.isDefPurchased() ) {
            // We need to create a new transaction for this purchase.
            transaction = new EntryModel();
            transaction.setAmount(purchase.getGallonsOfDef() 
                    * purchase.getPricePerGallonDef());
            transaction.setBalanced(false);
            transaction.setCode("DEF");
            transaction.setDate(purchase.getDate());
            transaction.setDeductible(true);
            transaction.setDescription(purchase.getGallonsOfDef() 
                    + " gallons at " + purchase.getLocation());
            transaction.setFromAccount(50040);
            transaction.setToAccount(10040);

            // If the user is using the batch posting method, we need to add this
            //+ purchase to the batch.
            if ( LoadMaster.batch != null ) 
                LoadMaster.batch.addNew(transaction);
        } else {
            // We need to get our general ledger and add this transaction to it.
            try {
                // Get our General Ledger.
                EntryCtl ledger = new EntryCtl();
                
                // Add our new transaction to it.
                ledger.addNew(transaction);
                
                // Close the General Ledger, thereby writing the data out to it.
                ledger.close();
            } catch ( DataStoreException ex ) {
                entry.setSourceMethodName("doSave");
                entry.setMessage(ex.getMessage());
                entry.setParameters(null);
                entry.setThrown(ex);
                Starter.logger.error(entry);
            }
        }
        
        // Leave as last statement.
        doClose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateLabel = new javax.swing.JLabel();
        datePicker = new org.jdesktop.swingx.JXDatePicker();
        idLabel = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        dieselPanel = new javax.swing.JPanel();
        dieselGallonsLabel = new javax.swing.JLabel();
        dieselGallonsField = new javax.swing.JFormattedTextField();
        dieselPriceLabel = new javax.swing.JLabel();
        dieselPriceField = new javax.swing.JFormattedTextField();
        defCheckBox = new javax.swing.JCheckBox();
        defPanel = new javax.swing.JPanel();
        defGallonsLabel = new javax.swing.JLabel();
        defGallonsField = new javax.swing.JFormattedTextField();
        defPriceLabel = new javax.swing.JLabel();
        defPriceField = new javax.swing.JFormattedTextField();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        notesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notesField = new javax.swing.JTextArea();
        odometerLabel = new javax.swing.JLabel();
        odometerField = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        dateLabel.setDisplayedMnemonic('D');
        dateLabel.setLabelFor(datePicker);
        dateLabel.setText("Date:");

        datePicker.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        datePicker.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        idLabel.setText("Purchase ID:");

        idField.setText(String.valueOf(System.currentTimeMillis()));
        idField.setEditable(false);
        idField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        idField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        locationLabel.setDisplayedMnemonic('L');
        locationLabel.setLabelFor(locationField);
        locationLabel.setText("Location:");

        locationField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        locationField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        dieselGallonsLabel.setDisplayedMnemonic('G');
        dieselGallonsLabel.setLabelFor(dieselGallonsField);
        dieselGallonsLabel.setText("Gallons Purchased:");

        dieselGallonsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        dieselGallonsField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        dieselGallonsField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        dieselPriceLabel.setDisplayedMnemonic('P');
        dieselPriceLabel.setLabelFor(dieselPriceField);
        dieselPriceLabel.setText("Price Per Gallon:");

        dieselPriceField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        dieselPriceField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        dieselPriceField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        javax.swing.GroupLayout dieselPanelLayout = new javax.swing.GroupLayout(dieselPanel);
        dieselPanel.setLayout(dieselPanelLayout);
        dieselPanelLayout.setHorizontalGroup(
            dieselPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dieselPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dieselGallonsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dieselGallonsField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dieselPriceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dieselPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        dieselPanelLayout.setVerticalGroup(
            dieselPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dieselPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dieselPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dieselGallonsLabel)
                    .addComponent(dieselGallonsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dieselPriceLabel)
                    .addComponent(dieselPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        defCheckBox.setMnemonic('E');
        defCheckBox.setText("DEF Purchased?");
        defCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                defCheckBoxItemStateChanged(evt);
            }
        });
        defCheckBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        defGallonsLabel.setDisplayedMnemonic('a');
        defGallonsLabel.setLabelFor(defGallonsField);
        defGallonsLabel.setText("Gallons Purchased:");

        defGallonsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        defGallonsField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        defGallonsField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        defPriceLabel.setDisplayedMnemonic('r');
        defPriceLabel.setLabelFor(dieselPriceField);
        defPriceLabel.setText("Price Per Gallon:");

        defPriceField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        defPriceField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectText(evt);
            }
        });
        defPriceField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        javax.swing.GroupLayout defPanelLayout = new javax.swing.GroupLayout(defPanel);
        defPanel.setLayout(defPanelLayout);
        defPanelLayout.setHorizontalGroup(
            defPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(defGallonsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(defGallonsField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(defPriceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(defPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        defPanelLayout.setVerticalGroup(
            defPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defGallonsLabel)
                    .addComponent(defGallonsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defPriceLabel)
                    .addComponent(defPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/pekinsoft/loadmaster/res/cancel16.png"))); // NOI18N
        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        okButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/pekinsoft/loadmaster/res/ok.png"))); // NOI18N
        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });

        notesLabel.setText("Notes:");

        notesField.setColumns(20);
        notesField.setRows(5);
        notesField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                checkForEscape(evt);
            }
        });
        jScrollPane1.setViewportView(notesField);

        odometerLabel.setDisplayedMnemonic('O');
        odometerLabel.setLabelFor(odometerField);
        odometerLabel.setText("Odometer:");

        odometerField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0"))));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(defCheckBox)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(defPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(locationLabel)
                                    .addComponent(dateLabel)
                                    .addComponent(odometerLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(locationField)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(idLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(odometerField, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addComponent(dieselPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(okButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(notesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateLabel)
                    .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idLabel)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(odometerLabel)
                    .addComponent(odometerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dieselPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(defCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(defPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notesLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkForEscape(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkForEscape
        // If the user types the ESCAPE key, we need do nothing except dispose 
        //+ of this dialog.
        if ( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE )
            doClose();
    }//GEN-LAST:event_checkForEscape

    private void defCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_defCheckBoxItemStateChanged
        // Keep the DEF Panel consistent with whether or not this is checked.
        defPanel.setVisible(defCheckBox.isSelected());
        pack(); // To automatically resize the dialog as necessary.
    }//GEN-LAST:event_defCheckBoxItemStateChanged

    private void selectText(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_selectText
        JTextComponent txt = null;
        if ( evt.getSource() instanceof JXDatePicker ) {
            txt = ((JXDatePicker)evt.getSource()).getEditor();
        } else if ( evt.getSource() instanceof JFormattedTextField ) {
            txt = (JFormattedTextField)evt.getSource();
        } else if ( evt.getSource() instanceof JTextField ) {
            txt = (JTextField)evt.getSource();
        }
        
        if ( txt != null ) 
            txt.selectAll();
    }//GEN-LAST:event_selectText

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel dateLabel;
    private org.jdesktop.swingx.JXDatePicker datePicker;
    private javax.swing.JCheckBox defCheckBox;
    private javax.swing.JFormattedTextField defGallonsField;
    private javax.swing.JLabel defGallonsLabel;
    private javax.swing.JPanel defPanel;
    private javax.swing.JFormattedTextField defPriceField;
    private javax.swing.JLabel defPriceLabel;
    private javax.swing.JFormattedTextField dieselGallonsField;
    private javax.swing.JLabel dieselGallonsLabel;
    private javax.swing.JPanel dieselPanel;
    private javax.swing.JFormattedTextField dieselPriceField;
    private javax.swing.JLabel dieselPriceLabel;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextArea notesField;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JFormattedTextField odometerField;
    private javax.swing.JLabel odometerLabel;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
