package sentiprofiler.ui.profile;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;

import sentiprofiler.Constants;
import sentiprofiler.profile.WNAffectOntology;
import sentiprofiler.utils.Utils;
import sentiprofiler.ui.*;
import sentiprofiler.ui.main.SPMainWindow;
import sentiprofiler.ui.ontology.OntologySelectionPanel;

/**
 * Dialog for creating sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class SPProfileCreatorDialog extends AbstractDialog  {
	protected SPMainWindow parent;
	protected FileSelectionPanel selPanel;
	protected OntologySelectionPanel ontPanel;
	
	public SPProfileCreatorDialog(SPMainWindow parent) {
		super("Create sentiment profile");
		this.parent = parent;
		ontPanel.update(parent.getProfiler().getProfileManager().getOntologies());
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	protected void createMainPanel() {
		final String testDir = Utils.getCurrentDirectory() + File.separator
			+ Constants.TEST_DATA_DIR;		
		createPanel(testDir);		
	}
	
	protected void createPanel(final String dir) {
		selPanel = new FileSelectionPanel(this, "Input file" ,dir);
		mainPanel.add(selPanel, BorderLayout.NORTH);
		ontPanel = new OntologySelectionPanel();
		mainPanel.add(ontPanel, BorderLayout.CENTER);
	}
		
	public void update() {
		mainPanel.removeAll();
		createPanel(selPanel.getText());
		ontPanel.update(parent.getProfiler().getProfileManager().getOntologies());
		mainPanel.revalidate();
		//tfInput.setCaretPosition(tfInput.getText().length());
	}

	
	protected void createButtonPanel() {
		createOkButton();
		createCancelButton();
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
	}	
	
	private void patchProcess(WNAffectOntology ontology, String files[]) {
		String dir = selPanel.getText();
		for(int x = 0; x < files.length; x++) {
			File file = new File(files[x]);
			if(!file.isDirectory() && !file.toString().equalsIgnoreCase("cvs")) {
				boolean ok = parent.getProfiler().analyzeAndOutput(ontology, dir, file.toString());
				if(ok)
					System.out.println("Processed file " + file.getName());
				else
					System.out.println("Failed to process " + file.getName());
			}
		}
	}
		
	protected Vector performTask() {		
	     setCursor(new Cursor(Cursor.WAIT_CURSOR));	     
	     if(!parent.getProfiler().isInitialized()) {
	    	 while(!parent.getProfiler().isInitialized()) {
	    		 Utils.pause(50);
	    	 }
	     }

	    WNAffectOntology ontology = ontPanel.getSelection();  
		if(ontology == null) {
			JOptionPane.showMessageDialog(this, Constants.NO_ONTOLOGY_SELECTED, "Warning", 
					JOptionPane.WARNING_MESSAGE);
			retVals = null;
		}
		File file = new File(selPanel.getText());
		if(file.isDirectory())
			patchProcess(ontology, file.list());
		else {		     
			boolean ok = parent.getProfiler().analyzeAndVisualize(ontology, selPanel.getText());
			if(!ok)
				JOptionPane.showMessageDialog(this, Constants.PROFILING_FAILED, "Warning", 
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
