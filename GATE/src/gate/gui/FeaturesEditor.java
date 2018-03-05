/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id: FeaturesEditor.java,v 1.1 2011/01/13 16:51:38 textmine Exp $
 *
 */

package gate.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import gate.Factory;
import gate.FeatureMap;
import gate.creole.AbstractVisualResource;
import gate.swing.XJTable;
import gate.util.FeatureBearer;

public class FeaturesEditor extends AbstractVisualResource{

  public FeaturesEditor() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }// FeaturesEditor()

  protected void initLocalData(){
    features = Factory.newFeatureMap();
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    tableModel = new FeaturesTableModel();
    table = new XJTable(tableModel);
//    table.setIntercellSpacing(new Dimension(5,5));
    table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
    table.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
    DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
    editor.setClickCountToStart(0);
    table.setDefaultEditor(String.class, editor);
    table.setDefaultEditor(Object.class, editor);

    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setOpaque(true);
    //the background colour seems to change somewhere when using the GTK+ 
    //look and feel on Linux, so we copy the value now and set it 
    Color tableBG = table.getBackground();
    //make a copy of the value (as the reference gets changed somewhere)
    tableBG = new Color(tableBG.getRGB());
    table.setBackground(tableBG);
    scroll.getViewport().setBackground(tableBG);
    this.add(scroll, BorderLayout.CENTER);
    this.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    newFeatureField = new JTextField(10);
    newFeatureField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                                 newFeatureField.
                                                 getPreferredSize().height));

    Box vBox = Box.createVerticalBox();
    vBox.add(new JLabel("New feature name"));
    vBox.add(newFeatureField);
    box.add(vBox);
    box.add(Box.createHorizontalStrut(5));

    newValueField = new JTextField(10);
    newValueField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                                 newValueField.
                                                 getPreferredSize().height));

    vBox = Box.createVerticalBox();
    vBox.add(new JLabel("New feature value"));
    vBox.add(newValueField);
    box.add(vBox);
    box.add(Box.createHorizontalStrut(5));

    addNewBtn = new JButton("Add feature");
    box.add(addNewBtn);
    box.add(Box.createHorizontalStrut(5));

    delBtn = new JButton("Delete");
    box.add(delBtn);

    this.add(box);
    this.add(Box.createVerticalGlue());

  }// protected void initGuiComponents()

  protected void initListeners(){
    addNewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = newFeatureField.getText();
        String value = newValueField.getText();
        if(name != null){
          features.put(name, value);
          tableModel.fireTableDataChanged();
          newFeatureField.setText("");
          newValueField.setText("");
        }
      }
    });

    delBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = newFeatureField.getText();
        String value = newValueField.getText();
        if(name != null){
          features.remove(name);
          tableModel.fireTableDataChanged();
          newFeatureField.setText("");
          newValueField.setText("");
        }
      }
    });
  }

  public void cleanup(){
    super.cleanup();
    features = null;
    resource = null;
  }

  public void setFeatureBearer(FeatureBearer newResource) {
    if(newResource == null){
      resource = null;
      features = null;
    }else{
      resource = newResource;
      features = resource.getFeatures();
    }
    tableModel.fireTableDataChanged();
  }// public void setFeatureBearer(FeatureBearer newResource)

  public void setTarget(Object target) {
    if(target == null || target instanceof FeatureBearer){
      setFeatureBearer((FeatureBearer)target);
    }else{
      throw new IllegalArgumentException(
        "FeatureEditors can only be used with FeatureBearer!\n" +
        target.getClass().toString() + " is not a FeatureBearer!");
    }
  }//public void setResource(Resource resource)

  public void setHandle(Handle handle){
    //NOP
  }


  public FeatureBearer getFeatureBearer() {
    return resource;
  }

  XJTable table;
  FeaturesTableModel tableModel;
  private FeatureBearer resource;
  FeatureMap features;
  JTextField newFeatureField;
  JTextField newValueField;

  JButton addNewBtn;
  JButton delBtn;

  class FeaturesTableModel extends AbstractTableModel{
    public int getColumnCount(){
      return 2;
    }

    public int getRowCount(){
      return features == null ? 0 : features.size();
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Feature";
        case 1: return "Value";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return String.class;
        case 1: return Object.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex,
                              int columnIndex){
      if(features == null) return false;
      return rowIndex == features.size()
             ||
             ((!((String)table.getModel().getValueAt(rowIndex, 0)).
              startsWith("gate."))
             );
    }// public boolean isCellEditable

    public Object getValueAt(int rowIndex,
                         int columnIndex){
      if(features == null) return null;
      List keys = new ArrayList(features.keySet());
      Collections.sort(keys);
      Object key = keys.get(rowIndex);
      switch(columnIndex){
        case 0:{
          return key;
        }
        case 1:{
          return features.get(key) == null ? "" : features.get(key).toString();
        }
        default:{
          return null;
        }
      }
    }// public Object getValueAt

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){

      if(columnIndex == 0) {
        //the name of the feature changed
        //if the name is null or empty the feature will be deleted
        String oldName = (String)getValueAt(rowIndex, 0);
        Object oldValue = features.remove(oldName);
        if(aValue != null && !aValue.equals("")){
          features.put(aValue, oldValue);
        }
      } else {
        //the value of a feature changed
        features.put(getValueAt(rowIndex, 0), aValue);
      }
      fireTableDataChanged();
    }// public void setValueAt

  }///class FeaturesTableModel extends DefaultTableModel
/*
  class FeaturesTableRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){

      super.getTableCellRendererComponent(table, value, false, hasFocus,
                                          row, column);
      setEnabled(table.isCellEditable(row, column));
      return this;
    }

  }// class FeaturesTableRenderer
*/
}// class FeaturesEditor