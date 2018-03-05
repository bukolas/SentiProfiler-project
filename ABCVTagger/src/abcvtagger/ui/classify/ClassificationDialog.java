package abcvtagger.ui.classify;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import abcvtagger.Constants;
import abcvtagger.ProfileAndOntologyManager;
import abcvtagger.profile.DocumentProfile;
import abcvtagger.ui.*;
import abcvtagger.ui.main.MainWindow;
import abcvtagger.ui.profile.ProfileTable;
import abcvtagger.utils.Utils;
import abvc.classifier.Classifier;



/**
 * Dialog for creating sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class ClassificationDialog extends AbstractDialog  {
	protected MainWindow parent;
	protected FileSelectionPanel selPanel;
	private JCheckBox jCBRunSA, jCBWriteInstanceCounts, jCBWriteClasses, jCBWriteFreq, jCBWriteFScore, jCBWriteSentiRatio, jCBWriteBoWFeatures ;
	protected Vector<DocumentProfile> p;
	
	public ClassificationDialog(MainWindow parent, Vector<DocumentProfile> profs) {
		super("Classifying profiles");
		this.parent = parent;
		this.p = profs;
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	protected void createMainPanel() {
		final String testDir = Utils.getCurrentDirectory() + File.separator
			+ Constants.TEST_DATA_DIR;		
		createPanel(testDir);		
	}
	
	protected void createPanel(final String dir) {
		//JPanel panel = new JPanel(new GridLayout(3, 3));
		JPanel panel = new JPanel(new GridLayout(2, 3));
		//JPanel panel2 = new JPanel(new GridLayout(1, 3));
		jCBRunSA = new JCheckBox("Run SentiStrength"); //write SA strength
		//jCBWriteInstanceCounts = new JCheckBox("Write # SP-Ontology Matches"); //Write ontology hit counts
		jCBWriteSentiRatio= new JCheckBox("Write SentiRatio");
		jCBWriteClasses = new JCheckBox("Write # OntoClass Matches"); //Write class hit counts
		//jCBWriteFreq = new JCheckBox("Write OntoClass Frequency");
		jCBWriteInstanceCounts = new JCheckBox("Write PosToNeg Ratio");
		jCBWriteFScore = new JCheckBox("Write FScore");
		jCBWriteBoWFeatures = new JCheckBox("Write emoBoW Features");
		
		
		panel.add(jCBWriteSentiRatio);
		panel.add(jCBWriteClasses);
		//panel.add(jCBWriteFreq);
		panel.add(jCBWriteInstanceCounts);
		panel.add(jCBRunSA);
		panel.add(jCBWriteFScore);
		panel.add(jCBWriteBoWFeatures);
		
		mainPanel.add(panel, BorderLayout.CENTER);
		//mainPanel.add(panel2, BorderLayout.CENTER);
	}
		
	public void update() {
		mainPanel.removeAll();
		createPanel(selPanel.getText());
		mainPanel.revalidate();
		//tfInput.setCaretPosition(tfInput.getText().length());
	}

	
	protected void createButtonPanel() {
		createOkButton();
		createCancelButton();
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
	}	
	
		
	protected Vector performTask() {		
	     setCursor(new Cursor(Cursor.WAIT_CURSOR));     
		ProfileAndOntologyManager profMan = parent.getProfiler().getProfileManager();
		//Vector<DocumentProfile> profs = ((ProfileTable) parent).getSelectedProfiles();
		
			Classifier classifier = new Classifier(profMan, p, jCBRunSA.isSelected(), 
					jCBWriteInstanceCounts.isSelected(),  jCBWriteClasses.isSelected(),
					jCBWriteFScore.isSelected(),jCBWriteSentiRatio.isSelected(),
					jCBWriteBoWFeatures.isSelected());
	
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		return retVals;
	    
	}
	 
}
