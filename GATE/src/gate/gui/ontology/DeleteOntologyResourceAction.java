/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: DeleteOntologyResourceAction.java,v 1.1 2011/01/13 16:52:05 textmine Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action to delete a resource from ontology.
 */
public class DeleteOntologyResourceAction extends AbstractAction implements
                                                                TreeNodeSelectionListener {
  private static final long serialVersionUID = 3257289136439439920L;

  public DeleteOntologyResourceAction(String caption, Icon icon) {
    super(caption, icon);
  }

  public void actionPerformed(ActionEvent actionevent) {
    String[] resourcesToDelete = new String[selectedNodes.size()];
    int i = 0;
    for (DefaultMutableTreeNode node : selectedNodes) {
      Object object = ((OResourceNode) node.getUserObject()).getResource();
      resourcesToDelete[i++] = ((OResource) object).getONodeID().toString();
    }
    JList list = new JList(resourcesToDelete);
    int choice = JOptionPane.showOptionDialog(MainFrame.getInstance(),
      new Object[]{"Are you sure you want to delete the following resources?",
      "\n\n", new JScrollPane(list), '\n'}, "Delete resources",
      JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
      new String[]{"Delete resources", "Cancel"}, "Cancel");
    if (choice == JOptionPane.CLOSED_OPTION || choice == 1)  { return; }
    for (DefaultMutableTreeNode node : selectedNodes) {
      Object object = ((OResourceNode) node.getUserObject()).getResource();
      try {
        if (object instanceof OClass) {
          if (ontology.containsOClass(((OClass) object).getURI()))
            ontology.removeOClass((OClass) object);
          continue;
        }
        if (object instanceof OInstance) {
          if (ontology.getOInstance(((OInstance) object).getURI()) != null)
            ontology.removeOInstance((OInstance) object);
          continue;
        }
        if ((object instanceof RDFProperty)
          && ontology.getOResourceFromMap(
            ((RDFProperty) object).getURI().toString()) != null)
          ontology.removeProperty((RDFProperty) object);
      }
      catch (Exception re) {
        re.printStackTrace();
        JOptionPane.showMessageDialog(MainFrame.getInstance(), re.getMessage() +
          "\nPlease see tab messages for more information!");
      }
    }
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology) {
    this.ontology = ontology;
  }

  public void selectionChanged(ArrayList<DefaultMutableTreeNode> arraylist) {
    selectedNodes = arraylist;
  }

  protected Ontology ontology;
  protected ArrayList<DefaultMutableTreeNode> selectedNodes;
}
