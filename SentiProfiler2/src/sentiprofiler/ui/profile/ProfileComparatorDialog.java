package sentiprofiler.ui.profile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import sentiprofiler.Constants;
import sentiprofiler.profile.WNAffectOntology;
import sentiprofiler.utils.RDFFileFilter;
import sentiprofiler.utils.TextFileFilter;
import sentiprofiler.utils.Utils;
import sentiprofiler.ui.*;
import sentiprofiler.ui.main.SPMainWindow;
import sentiprofiler.ui.ontology.OntologySelectionPanel;

/**
 * Dialog for creating sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class ProfileComparatorDialog extends AbstractDialog {
	private FileSelectionPanel selPanel, selPanel2;
	protected SPMainWindow parent;
	protected OntologySelectionPanel ontPanel;
	
	public ProfileComparatorDialog(SPMainWindow parent) {
		super("Create and compare sentiment profiles");
		this.parent = parent;
		ontPanel.update(parent.getProfiler().getProfileManager().getOntologies());
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	protected void createMainPanel() {
		final String testDir = Utils.getCurrentDirectory() + File.separator
			+ Constants.TEST_DATA_DIR;
		
		createPanel(testDir, testDir);		
	}
	
	protected void createPanel(final String dir, final String dir2) {
		selPanel = new FileSelectionPanel(this, "Input file 1" ,dir);
		selPanel2 = new FileSelectionPanel(this, "Input file 2" ,dir2);
		JPanel filePanel = new JPanel(new BorderLayout());
		filePanel.add(selPanel, BorderLayout.NORTH);
		filePanel.add(selPanel2, BorderLayout.CENTER);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(filePanel, BorderLayout.NORTH);
		ontPanel = new OntologySelectionPanel();
		mainPanel.add(ontPanel, BorderLayout.CENTER);

	}
		
	public void update() {
		mainPanel.removeAll();
		createPanel(selPanel.getText(), selPanel2.getText());
		ontPanel.update(parent.getProfiler().getProfileManager().getOntologies());
		mainPanel.revalidate();
	}

	
	protected void createButtonPanel() {
		createOkButton();
		createCancelButton();
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
	}	
	
		
	protected Vector performTask() { 
	     setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    WNAffectOntology ontology = ontPanel.getSelection();  
		if(ontology == null) {
			JOptionPane.showMessageDialog(this, Constants.NO_ONTOLOGY_SELECTED, "Warning", 
					JOptionPane.WARNING_MESSAGE);
			retVals = null;
		}
		else {
			//System.out.println("selPanel " + selPanel.getText() + " selPanel2 " + selPanel2.getText());
			boolean ok = parent.getProfiler().createAndCompareProfiles(ontology,
					selPanel.getText(), selPanel2.getText());
			if(!ok)
				JOptionPane.showMessageDialog(parent, Constants.PROFILING_FAILED, "Warning", 
						JOptionPane.WARNING_MESSAGE);
			else {
				retVals = new Vector();
				retVals.add("");
			}		     
		}		
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		return retVals;
	}
	 
}
