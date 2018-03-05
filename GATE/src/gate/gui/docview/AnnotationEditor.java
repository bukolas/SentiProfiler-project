/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationEditor.java
 *
 *  Valentin Tablan, Apr 5, 2004
 *
 *  $Id: AnnotationEditor.java,v 1.1 2011/01/13 16:52:10 textmine Exp $
 */

package gate.gui.docview;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;

import gate.*;
import gate.creole.*;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.FeaturesSchemaEditor;
import gate.gui.MainFrame;
import gate.gui.annedit.*;
import gate.util.*;


/**
 * A generic annotation editor, which uses the known annotation schemas to help
 * speed up the annotation process (e.g. by pre-populating sets of choices) but
 * does not enforce the schemas, allowing the user full control.
 */
public class AnnotationEditor extends AbstractVisualResource 
    implements OwnedAnnotationEditor{
  
  private static final long serialVersionUID = 1L;

  /* (non-Javadoc)
   * @see gate.creole.AbstractVisualResource#init()
   */
  @Override
  public Resource init() throws ResourceInstantiationException {

    super.init();
    initData();
    initGUI();
    initListeners();
    annotationEditorInstance = this;
    return this;
  }

  protected void initData(){
    schemasByType = new HashMap<String, AnnotationSchema>();
    java.util.List schemas = Gate.getCreoleRegister().
        getLrInstances("gate.creole.AnnotationSchema");
    for(Iterator schIter = schemas.iterator(); 
        schIter.hasNext();){
      AnnotationSchema aSchema = (AnnotationSchema)schIter.next();
      schemasByType.put(aSchema.getAnnotationName(), aSchema);
    }
    
    CreoleListener creoleListener = new CreoleListener(){
      public void resourceLoaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
  	      AnnotationSchema aSchema = (AnnotationSchema)newResource;
  	      schemasByType.put(aSchema.getAnnotationName(), aSchema);
        }
      }
      
      public void resourceUnloaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
  	      AnnotationSchema aSchema = (AnnotationSchema)newResource;
  	      if(schemasByType.containsValue(aSchema)){
  	        schemasByType.remove(aSchema.getAnnotationName());
  	      }
        }
      }
      
      public void datastoreOpened(CreoleEvent e){
        
      }
      public void datastoreCreated(CreoleEvent e){
        
      }
      public void datastoreClosed(CreoleEvent e){
        
      }
      public void resourceRenamed(Resource resource,
                              String oldName,
                              String newName){
      }  
    };
    Gate.getCreoleRegister().addCreoleListener(creoleListener); 
  }
  
  protected void initGUI(){

    popupWindow = new JWindow(SwingUtilities.getWindowAncestor(
        owner.getTextComponent())) {
      public void pack() {
        // increase the feature table size only if not bigger
        // than the main frame
        if(isVisible()){
          int maxHeight = MainFrame.getInstance().getHeight();
          int otherHeight = getHeight() - featuresScroller.getHeight();
          maxHeight -= otherHeight;
          if(featuresScroller.getPreferredSize().height > maxHeight){
            featuresScroller.setMaximumSize(new Dimension(
                    featuresScroller.getMaximumSize().width, maxHeight));
            featuresScroller.setPreferredSize(new Dimension(
                    featuresScroller.getPreferredSize().width, maxHeight));
          }
          
        }
        super.pack();
      }
      public void setVisible(boolean b) {
        super.setVisible(b);
        // when the editor is shown put the focus in the type combo box
        if (b) { typeCombo.requestFocus(); }
      }
    };

    JPanel pane = new JPanel();
    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    pane.setLayout(new GridBagLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    popupWindow.setContentPane(pane);

    Insets insets0 = new Insets(0, 0, 0, 0);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridwidth = 1;
    constraints.gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.weightx = 0;
    constraints.weighty= 0;
    constraints.insets = insets0;

    solButton = new JButton();
    solButton.setContentAreaFilled(false);
    solButton.setBorderPainted(false);
    solButton.setMargin(insets0);
    pane.add(solButton, constraints);
    
    sorButton = new JButton();
    sorButton.setContentAreaFilled(false);
    sorButton.setBorderPainted(false);
    sorButton.setMargin(insets0);
    pane.add(sorButton, constraints);
    
    delButton = new JButton();
    delButton.setContentAreaFilled(false);
    delButton.setBorderPainted(false);
    delButton.setMargin(insets0);
    constraints.insets = new Insets(0, 20, 0, 20);
    pane.add(delButton, constraints);
    constraints.insets = insets0;
    
    eolButton = new JButton();
    eolButton.setContentAreaFilled(false);
    eolButton.setBorderPainted(false);
    eolButton.setMargin(insets0);
    pane.add(eolButton, constraints);
    
    eorButton = new JButton();
    eorButton.setContentAreaFilled(false);
    eorButton.setBorderPainted(false);
    eorButton.setMargin(insets0);
    pane.add(eorButton, constraints);
    
    pinnedButton = new JToggleButton(MainFrame.getIcon("pin"));
    pinnedButton.setSelectedIcon(MainFrame.getIcon("pin-in"));
    pinnedButton.setSelected(false);
    pinnedButton.setBorderPainted(false);
    pinnedButton.setContentAreaFilled(false);
    constraints.weightx = 1;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.anchor = GridBagConstraints.EAST;
    pane.add(pinnedButton, constraints);

    dismissButton = new JButton();
    dismissButton.setBorder(null);
    constraints.anchor = GridBagConstraints.NORTHEAST;
    pane.add(dismissButton, constraints);
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = insets0;

    
    typeCombo = new JComboBox();
    typeCombo.setEditable(true);
    typeCombo.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 1;
    constraints.gridwidth = 7;
    constraints.weightx = 1;
    constraints.insets = new Insets(3, 2, 2, 2);
    pane.add(typeCombo, constraints);
    
    featuresEditor = new FeaturesSchemaEditor();
    featuresEditor.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    try{
      featuresEditor.init();
    }catch(ResourceInstantiationException rie){
      throw new GateRuntimeException(rie);
    }

    constraints.gridy = 2;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    featuresScroller = new JScrollPane(featuresEditor); 
    pane.add(featuresScroller, constraints);

    // add the search and annotate GUI at the bottom of the annotator editor
    SearchAndAnnotatePanel searchPanel =
      new SearchAndAnnotatePanel(pane.getBackground(), this, popupWindow);
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.gridheight = GridBagConstraints.REMAINDER;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    pane.add(searchPanel, constraints);

    popupWindow.pack();
  }

  protected void initListeners(){
    //resize the window when the table changes.
    featuresEditor.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        //the table has changed size -> resize the window too! 
        popupWindow.pack();
      }
    });

    KeyAdapter keyAdapter = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        hideTimer.stop();
      }
    };

    typeCombo.getEditor().getEditorComponent().addKeyListener(keyAdapter);

    MouseListener windowMouseListener = new MouseAdapter() {
      public void mouseEntered(MouseEvent evt) {
        hideTimer.stop();
      }
      // allow a JWindow to be dragged with a mouse
      public void mousePressed(MouseEvent me) {
        pressed = me;
      }
    };

    MouseMotionListener windowMouseMotionListener = new MouseMotionAdapter() {
      Point location;
      // allow a JWindow to be dragged with a mouse
      public void mouseDragged(MouseEvent me) {
        location = popupWindow.getLocation(location);
        int x = location.x - pressed.getX() + me.getX();
        int y = location.y - pressed.getY() + me.getY();
        popupWindow.setLocation(x, y);
        pinnedButton.setSelected(true);
       }
    };

    popupWindow.getRootPane().addMouseListener(windowMouseListener);
    popupWindow.getRootPane().addMouseMotionListener(windowMouseMotionListener);

    InputMap inputMap = ((JComponent) popupWindow.getContentPane()).
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    actionMap = ((JComponent)popupWindow.getContentPane()).getActionMap();

    // add the key-action bindings of this Component to the parent window
    solAction =
      new StartOffsetLeftAction("", MainFrame.getIcon("extend-left"),
      "<html><b>Extend start</b><small>" +
      "<br>LEFT = 1 character" +
      "<br> + SHIFT = 5 characters, "+
      "<br> + CTRL + SHIFT = 10 characters</small></html>",
       KeyEvent.VK_LEFT);
    solButton.setAction(solAction);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), "solAction");
    inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), "solAction");
    inputMap.put(KeyStroke.getKeyStroke("control shift released LEFT"),
      "solAction");
    actionMap.put("solAction", solAction);

   sorAction =
     new StartOffsetRightAction("", MainFrame.getIcon("extend-right"),
      "<html><b>Shrink start</b><small>" +
      "<br>RIGHT = 1 character" +
      "<br> + SHIFT = 5 characters, "+
      "<br> + CTRL + SHIFT = 10 characters</small></html>",
      KeyEvent.VK_RIGHT);
    sorButton.setAction(sorAction);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "sorAction");
    inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), "sorAction");
    inputMap.put(KeyStroke.getKeyStroke("control shift released RIGHT"),
      "sorAction");
    actionMap.put("sorAction", sorAction);

    delAction =
      new DeleteAnnotationAction("", MainFrame.getIcon("remove-annotation"),
      "Delete the annotation", KeyEvent.VK_DELETE);
    delButton.setAction(delAction);
    inputMap.put(KeyStroke.getKeyStroke("alt DELETE"), "delAction");
    actionMap.put("delAction", delAction);

    eolAction =
      new EndOffsetLeftAction("", MainFrame.getIcon("extend-left"),
      "<html><b>Shrink end</b><small>" +
      "<br>ALT + LEFT = 1 character" +
      "<br> + SHIFT = 5 characters, "+
      "<br> + CTRL + SHIFT = 10 characters</small></html>",
       KeyEvent.VK_LEFT);
    eolButton.setAction(eolAction);
    inputMap.put(KeyStroke.getKeyStroke("alt LEFT"), "eolAction");
    inputMap.put(KeyStroke.getKeyStroke("alt shift LEFT"), "eolAction");
    inputMap.put(KeyStroke.getKeyStroke("control alt shift released LEFT"),
      "eolAction");
    actionMap.put("eolAction", eolAction);

    eorAction =
      new EndOffsetRightAction("", MainFrame.getIcon("extend-right"),
      "<html><b>Extend end</b><small>" +
      "<br>ALT + RIGHT = 1 character" +
      "<br> + SHIFT = 5 characters, "+
      "<br> + CTRL + SHIFT = 10 characters</small></html>",
      KeyEvent.VK_RIGHT);
    eorButton.setAction(eorAction);
    inputMap.put(KeyStroke.getKeyStroke("alt RIGHT"), "eorAction");
    inputMap.put(KeyStroke.getKeyStroke("alt shift RIGHT"), "eorAction");
    inputMap.put(KeyStroke.getKeyStroke("control alt shift released RIGHT"),
      "eorAction");
    actionMap.put("eorAction", eorAction);

    pinnedButton.setToolTipText("<html>Press to pin window in place"
      + "&nbsp;&nbsp;<font color=#667799><small>Ctrl-P"
      + "&nbsp;&nbsp;</small></font></html>");
    inputMap.put(KeyStroke.getKeyStroke("control P"), "toggle pin");
    actionMap.put("toggle pin", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        pinnedButton.doClick();
      }
    });

    DismissAction dismissAction = new DismissAction("", null,
      "Close the window", KeyEvent.VK_ESCAPE);
    dismissButton.setAction(dismissAction);
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "dismissAction");
    inputMap.put(KeyStroke.getKeyStroke("alt ESCAPE"), "dismissAction");
    actionMap.put("dismissAction", dismissAction);

    ApplyAction applyAction =
      new ApplyAction("Apply", null, "", KeyEvent.VK_ENTER);
    inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), "applyAction");
    actionMap.put("applyAction", applyAction);

    typeCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String newType = typeCombo.getSelectedItem().toString();
        if(ann == null || ann.getType().equals(newType)) return;
        //annotation editing
        Integer oldId = ann.getId();
        Annotation oldAnn = ann;
        set.remove(ann);
        try{
          set.add(oldId, oldAnn.getStartNode().getOffset(), 
                  oldAnn.getEndNode().getOffset(), 
                  newType, oldAnn.getFeatures());
          Annotation newAnn = set.get(oldId);
          //update the selection to the new annotation
          getOwner().selectAnnotation(new AnnotationDataImpl(set, newAnn));
          editAnnotation(newAnn, set);
          owner.annotationChanged(newAnn, set, oldAnn.getType());
        }catch(InvalidOffsetException ioe){
          throw new GateRuntimeException(ioe);
        }
      }
    });

    hideTimer = new Timer(HIDE_DELAY, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        annotationEditorInstance.setVisible(false);
      }
    });
    hideTimer.setRepeats(false);

    AncestorListener textAncestorListener = new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        if(wasShowing) {
          annotationEditorInstance.setVisible(true);
        }
        wasShowing = false;
      }

      public void ancestorRemoved(AncestorEvent event) {
        if(isShowing()) {
          wasShowing = true;
          popupWindow.dispose();
        }
      }

      public void ancestorMoved(AncestorEvent event) {
      }

      private boolean wasShowing = false;
    };
    owner.getTextComponent().addAncestorListener(textAncestorListener);
  }

  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditor#isActive()
   */
  public boolean isActive() {
    return popupWindow.isVisible();
  }

  public void editAnnotation(Annotation ann, AnnotationSet set){
    this.ann = ann;
    this.set = set;
    if (ann == null) {
      typeCombo.setModel(new DefaultComboBoxModel());
      featuresEditor.setSchema(new AnnotationSchema());
//      popupWindow.doLayout();
      popupWindow.validate();
      return;
    }
    //repopulate the types combo
    String annType = ann.getType();
    Set<String> types = new HashSet<String>(schemasByType.keySet());
    types.add(annType);
    types.addAll(set.getAllTypes());
    java.util.List<String> typeList = new ArrayList<String>(types);
    Collections.sort(typeList);
    typeCombo.setModel(new DefaultComboBoxModel(typeList.toArray()));
    typeCombo.setSelectedItem(annType);
   
    featuresEditor.setSchema(schemasByType.get(annType));
    featuresEditor.setTargetFeatures(ann.getFeatures());
//    popupWindow.doLayout();
    popupWindow.validate();
    setEditingEnabled(true);
    setVisible(true);
    if (!pinnedButton.isSelected()) {
      hideTimer.restart();
    }
  }

  public Annotation getAnnotationCurrentlyEdited() {
    return ann;
  }
  
  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditor#editingFinished()
   */
  public boolean editingFinished() {
    //this editor implementation has no special requirements (such as schema 
    //compliance), so it always returns true.
    return true;
  }

  public boolean isShowing(){
    return popupWindow.isShowing();
  }
  
  /**
   * Shows/Hides the UI(s) involved in annotation editing.
   */
  @Override
  public void setVisible(boolean setVisible) {
    super.setVisible(setVisible);
    if (setVisible) {
      placeDialog(ann.getStartNode().getOffset().intValue(),
        ann.getEndNode().getOffset().intValue());

    } else {
      popupWindow.setVisible(false);
      pinnedButton.setSelected(false);
      SwingUtilities.invokeLater(new Runnable() { public void run() {
        // when hiding the editor put back the focus in the document
        owner.getTextComponent().requestFocus();
      }});
    }
  }

  /**
   * Finds the best location for the editor dialog for a given span of text.
   */
  public void placeDialog(int start, int end){
    if(popupWindow.isVisible() && pinnedButton.isSelected()){
      //just resize
      Point where = popupWindow.getLocation();
      popupWindow.pack();
      if(where != null){
        popupWindow.setLocation(where);
      }
    }else{
      //calculate position
      try{
        Rectangle startRect = owner.getTextComponent().modelToView(start);
        Rectangle endRect = owner.getTextComponent().modelToView(end);
        Point topLeft = owner.getTextComponent().getLocationOnScreen();
        int x = topLeft.x + startRect.x;
        int y = topLeft.y + endRect.y + endRect.height;

        //make sure the window doesn't start lower 
        //than the end of the visible rectangle
        Rectangle visRect = owner.getTextComponent().getVisibleRect();
        int maxY = topLeft.y + visRect.y + visRect.height;      
        
        //make sure window doesn't get off-screen       
        popupWindow.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        boolean revalidate = false;
        if(popupWindow.getSize().width > screenSize.width){
          popupWindow.setSize(screenSize.width, popupWindow.getSize().height);
          revalidate = true;
        }
        if(popupWindow.getSize().height > screenSize.height){
          popupWindow.setSize(popupWindow.getSize().width, screenSize.height);
          revalidate = true;
        }
        
        if(revalidate) popupWindow.validate();
        //calculate max X
        int maxX = screenSize.width - popupWindow.getSize().width;
        //calculate max Y
        if(maxY + popupWindow.getSize().height > screenSize.height){
          maxY = screenSize.height - popupWindow.getSize().height;
        }
        
        //correct position
        if(y > maxY) y = maxY;
        if(x > maxX) x = maxX;
        popupWindow.setLocation(x, y);
      }catch(BadLocationException ble){
        //this should never occur
        throw new GateRuntimeException(ble);
      }
    }
    if(!popupWindow.isVisible()) popupWindow.setVisible(true);
  }
  
  /**
   * Changes the span of an existing annotation by creating a new annotation 
   * with the same ID, type and features but with the new start and end offsets.
   * @param set the annotation set 
   * @param oldAnnotation the annotation to be moved
   * @param newStartOffset the new start offset
   * @param newEndOffset the new end offset
   */
  protected void moveAnnotation(AnnotationSet set, Annotation oldAnnotation, 
          Long newStartOffset, Long newEndOffset) throws InvalidOffsetException{
    //Moving is done by deleting the old annotation and creating a new one.
    //If this was the last one of one type it would mess up the gui which 
    //"forgets" about this type and then it recreates it (with a different 
    //colour and not visible.
    //In order to avoid this problem, we'll create a new temporary annotation.
    Annotation tempAnn = null;
    if(set.get(oldAnnotation.getType()).size() == 1){
      //create a clone of the annotation that will be deleted, to act as a 
      //placeholder 
      Integer tempAnnId = set.add(oldAnnotation.getStartNode(), 
              oldAnnotation.getStartNode(), oldAnnotation.getType(), 
              oldAnnotation.getFeatures());
      tempAnn = set.get(tempAnnId);
    }

    Integer oldID = oldAnnotation.getId();
    set.remove(oldAnnotation);
    set.add(oldID, newStartOffset, newEndOffset,
            oldAnnotation.getType(), oldAnnotation.getFeatures());
    Annotation newAnn = set.get(oldID);
    //update the selection to the new annotation
    getOwner().selectAnnotation(new AnnotationDataImpl(set, newAnn));

    editAnnotation(newAnn, set);
    //remove the temporary annotation
    if(tempAnn != null) set.remove(tempAnn);
    owner.annotationChanged(newAnn, set, null);
  }   
  
  /**
   * Base class for actions on annotations.
   */
  protected abstract class AnnotationAction extends AbstractAction{
    public AnnotationAction(String text, Icon icon,
                            String desc, int mnemonic){
      super(text, icon);
      putValue(SHORT_DESCRIPTION, desc);
      putValue(MNEMONIC_KEY, mnemonic);
    }
  }

  protected class StartOffsetLeftAction extends AnnotationAction{
    private static final long serialVersionUID = 1L;
    public StartOffsetLeftAction(String text, Icon icon,
                                 String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
    }
    public void actionPerformed(ActionEvent evt){
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = ann.getStartNode().getOffset().longValue() - increment;
      if(newValue < 0) newValue = 0;
      try{
        moveAnnotation(set, ann, new Long(newValue), 
                ann.getEndNode().getOffset());
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class StartOffsetRightAction extends AnnotationAction{
    private static final long serialVersionUID = 1L;
    public StartOffsetRightAction(String text, Icon icon,
                                 String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
    }
    public void actionPerformed(ActionEvent evt){
      long endOffset = ann.getEndNode().getOffset().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = ann.getStartNode().getOffset().longValue()  + increment;
      if(newValue > endOffset) newValue = endOffset;
      try{
        moveAnnotation(set, ann, new Long(newValue), 
                ann.getEndNode().getOffset());
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }

  protected class EndOffsetLeftAction extends AnnotationAction{
    private static final long serialVersionUID = 1L;
    public EndOffsetLeftAction(String text, Icon icon,
                                 String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
    }
    public void actionPerformed(ActionEvent evt){
      long startOffset = ann.getStartNode().getOffset().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment =CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = ann.getEndNode().getOffset().longValue()  - increment;
      if(newValue < startOffset) newValue = startOffset;
      try{
        moveAnnotation(set, ann, ann.getStartNode().getOffset(), 
                new Long(newValue));
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class EndOffsetRightAction extends AnnotationAction{
    private static final long serialVersionUID = 1L;
    public EndOffsetRightAction(String text, Icon icon,
                                 String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
    }
    public void actionPerformed(ActionEvent evt){
      long maxOffset = owner.getDocument().getContent().size().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = ann.getEndNode().getOffset().longValue() + increment;
      if(newValue > maxOffset) newValue = maxOffset;
      try{
        moveAnnotation(set, ann, ann.getStartNode().getOffset(),
                new Long(newValue));
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class DeleteAnnotationAction extends AnnotationAction{
   private static final long serialVersionUID = 1L;
    public DeleteAnnotationAction(String text, Icon icon,
                                 String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
    }
    public void actionPerformed(ActionEvent evt){
      set.remove(ann);

      //clear the dialog
      editAnnotation(null, set);

      if(!pinnedButton.isSelected()){
        //if not pinned, hide the dialog.
        annotationEditorInstance.setVisible(false);
      } else {
        setEditingEnabled(false);
      }
    }
  }
  
  protected class DismissAction extends AnnotationAction{
    private static final long serialVersionUID = 1L;
    public DismissAction(String text, Icon icon,
                       String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
      Icon exitIcon = UIManager.getIcon("InternalFrame.closeIcon");
      if(exitIcon == null) exitIcon = MainFrame.getIcon("exit");
      putValue(SMALL_ICON, exitIcon);
    }
    public void actionPerformed(ActionEvent evt){
      annotationEditorInstance.setVisible(false);
    }
  }
  
  protected class ApplyAction extends AnnotationAction{
    private static final long serialVersionUID = 1L;
    public ApplyAction(String text, Icon icon,
                       String desc, int mnemonic) {
      super(text, icon, desc, mnemonic);
    }
    public void actionPerformed(ActionEvent evt){
      annotationEditorInstance.setVisible(false);
    }
  }
  
  /**
   * The popup window used by the editor.
   */
  protected JWindow popupWindow;

  /**
   * Toggle button used to pin down the dialog. 
   */
  protected JToggleButton pinnedButton;
  
  /**
   * Combobox for annotation type.
   */
  protected JComboBox typeCombo;
  
  /**
   * Component for features editing.
   */
  protected FeaturesSchemaEditor featuresEditor;
  
  protected JScrollPane featuresScroller;
  
  
  protected JButton solButton;
  protected JButton sorButton;
  protected JButton delButton;
  protected JButton eolButton;
  protected JButton eorButton;
  protected JButton dismissButton;
  
  protected Timer hideTimer;
  protected MouseEvent pressed;

  /**
   * Constant for delay before hiding the popup window (in milliseconds).
   */
  protected static final int HIDE_DELAY = 1500;
  
  /**
   * Constant for the number of characters when changing annotation boundary 
   * with Shift key pressed.
   */
  protected static final int SHIFT_INCREMENT = 5;

  /**
   * Constant for the number of characters when changing annotation boundary 
   * with Ctrl+Shift keys pressed.
   */
  protected static final int CTRL_SHIFT_INCREMENT = 10;
  
  /**
   * Stores the Annotation schema objects available in the system.
   * The annotation types are used as keys for the map.
   */
  protected Map<String, AnnotationSchema> schemasByType;
  
  /**
   * The controlling object for this editor.
   */
  private AnnotationEditorOwner owner;

  /**
   * The annotation being edited.
   */
  protected Annotation ann;
  
  /**
   * The parent set of the current annotation.
   */
  protected AnnotationSet set;

  /**
   * Current instance of this class.
   */
  protected AnnotationEditor annotationEditorInstance;

  /**
   * Action bindings for the popup window.
   */
  private ActionMap actionMap;

  private StartOffsetLeftAction solAction;
  private StartOffsetRightAction sorAction;
  private DeleteAnnotationAction delAction;
  private EndOffsetLeftAction eolAction;
  private EndOffsetRightAction eorAction;

  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditor#getAnnotationSetCurrentlyEdited()
   */
  public AnnotationSet getAnnotationSetCurrentlyEdited() {
    return set;
  }
  
  /**
   * @return the owner
   */
  public AnnotationEditorOwner getOwner() {
    return owner;
  }

  /**
   * @param owner the owner to set
   */
  public void setOwner(AnnotationEditorOwner owner) {
    this.owner = owner;
  }

  public void setPinnedMode(boolean pinned) {
    pinnedButton.setSelected(pinned);
  }

  public void setEditingEnabled(boolean isEditingEnabled) {
    solButton.setEnabled(isEditingEnabled);
    sorButton.setEnabled(isEditingEnabled);
    delButton.setEnabled(isEditingEnabled);
    eolButton.setEnabled(isEditingEnabled);
    eorButton.setEnabled(isEditingEnabled);
    typeCombo.setEnabled(isEditingEnabled);
    // cancel editing, if any
    if (featuresEditor.isEditing()) {
      featuresEditor.getColumnModel()
        .getColumn(featuresEditor.getEditingColumn())
        .getCellEditor().cancelCellEditing();
    }
   // en/disable the featuresEditor table, no easy way unfortunately : |
    featuresEditor.setEnabled(isEditingEnabled);
    if (isEditingEnabled) {
      // avoid the background to be incorrectly reset to the default color
      Color tableBG = featuresEditor.getBackground();
      tableBG = new Color(tableBG.getRGB());
      featuresEditor.setBackground(tableBG);
    }
    final boolean isEditingEnabledF = isEditingEnabled;
    for (int col = 0;
         col < featuresEditor.getColumnCount(); col++) {
      final TableCellRenderer previousTcr = featuresEditor
        .getColumnModel().getColumn(col).getCellRenderer();
      TableCellRenderer tcr = new TableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
        Component c = previousTcr.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
        c.setEnabled(isEditingEnabledF);
        return c;
      }
    };
    featuresEditor.getColumnModel().getColumn(col).setCellRenderer(tcr);
    }
    // enable/disable the key binding actions
    if (isEditingEnabled) {
      actionMap.put("solAction", solAction);
      actionMap.put("sorAction", sorAction);
      actionMap.put("delAction", delAction);
      actionMap.put("eolAction", eolAction);
      actionMap.put("eorAction", eorAction);
    } else {
      actionMap.put("solAction", null);
      actionMap.put("sorAction", null);
      actionMap.put("delAction", null);
      actionMap.put("eolAction", null);
      actionMap.put("eorAction", null);
    }
  }

  /**
   * Does nothing, as this editor does not support cancelling and rollbacks.
   */
  public void cancelAction() throws GateException {
  }

  /**
   * Returns <tt>true</tt> always as this editor is generic and can edit any
   * annotation type.
   */
  public boolean canDisplayAnnotationType(String annotationType) {
    return true;
  }

  /**
   * Does nothing as this editor works in auto-commit mode (changes are 
   * implemented immediately).
   */
  public void okAction() throws GateException {
  }

  /**
   * Returns <tt>false</tt>, as this editor does not support cancel operations.
   */
  public boolean supportsCancel() {
    return false;
  }
}
