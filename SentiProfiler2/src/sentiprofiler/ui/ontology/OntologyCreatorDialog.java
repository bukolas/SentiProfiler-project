package sentiprofiler.ui.ontology;

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
import sentiprofiler.utils.RDFFileFilter;
import sentiprofiler.utils.TextFileFilter;
import sentiprofiler.utils.Utils;
import sentiprofiler.ui.*;
import sentiprofiler.ui.main.SPMainWindow;

/**
 * Dialog for creating ontologies.
 * @author Tuomo Kakkonen
 */
public class OntologyCreatorDialog extends AbstractDialog {
	private FileSelectionPanel selPanel, selPanel2;
	protected SPMainWindow parent;
	
	public OntologyCreatorDialog(SPMainWindow parent) {
		super("Create ontology");
		this.parent = parent;
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	protected void createMainPanel() {
		final String testDir = Utils.getCurrentDirectory() + File.separator
			+ Constants.TEST_DATA_DIR;
		
		createPanel(testDir, testDir);		
	}
	
	protected void createPanel(final String dir, final String dir2) {
		selPanel = new FileSelectionPanel(this, "Hierarchy file" ,dir);
		selPanel2 = new FileSelectionPanel(this, "Ontology file" ,dir2);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(selPanel, BorderLayout.NORTH);
		mainPanel.add(selPanel2, BorderLayout.CENTER);
	}
		
	public void update() {
		mainPanel.removeAll();
		createPanel(selPanel.getText(), selPanel2.getText());
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
	     
		/*boolean ok = parent.getProfiler().createAndCompareProfiles(
				selPanel.getText(), selPanel2.getText());*/
		//if(!ok)
			JOptionPane.showMessageDialog(parent, Constants.PROFILING_FAILED, "Warning", 
					JOptionPane.WARNING_MESSAGE);

	     setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		return retVals;
	}
	 
}
