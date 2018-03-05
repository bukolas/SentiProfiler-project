package abcvtagger.ui.profile;

import gate.Document;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import abcv.demo.ui.DemoUI;
import abcvtagger.Constants;
import abcvtagger.profile.DocumentProfile;
import abcvtagger.ui.*;
import abcvtagger.ui.classify.ClassificationDialog;
import abcvtagger.ui.main.MainWindow;
import abcvtagger.utils.Utils;



/**
 * Dialog for creating sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class ProfileCreatorDialog extends AbstractDialog  {
	protected MainWindow parent;
	protected FileSelectionPanel selPanel;
	private JCheckBox jCheckVisualize, jCheckProcessSubFolders, jCheckClassifiers;
	private JTextField jTextCategory;
	protected DemoUI demo;

	
	public ProfileCreatorDialog(MainWindow parent) {
		super("Create document profile");
		this.parent = parent;
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	public ProfileCreatorDialog(DemoUI demo) {
		super("");
	}
	protected void createMainPanel() {
	//	final String testDir = Utils.getCurrentDirectory() + File.separator
	//		+ Constants.TEST_DATA_DIR;		
		final String testDir = Utils.getCurrentDirectory();
		createPanel(testDir);		
	}
	
	protected void createPanel(final String dir) {
		selPanel = new FileSelectionPanel(this, "Input file" ,dir);
		mainPanel.add(selPanel, BorderLayout.NORTH);
		
		JLabel label = new JLabel("Category name");
		jTextCategory = new JTextField(20);
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(jTextCategory);
		mainPanel.add(panel, BorderLayout.CENTER);				
		
		JPanel checkBoxPanel = new JPanel();
		
		jCheckVisualize = new JCheckBox("Open in visualizer");
		checkBoxPanel.add(jCheckVisualize);
		
		jCheckProcessSubFolders = new JCheckBox("Process sub-foldres as categories");
		checkBoxPanel.add(jCheckProcessSubFolders);

		jCheckClassifiers = new JCheckBox("Run classifiers");
		checkBoxPanel.add(jCheckClassifiers);
		
		mainPanel.add(checkBoxPanel, BorderLayout.SOUTH);

	}

	private void categorize(Vector<DocumentProfile> profs) {
		ClassificationDialog cd = new ClassificationDialog(parent, profs);
		cd.showDialog();
	}
	
	public void update() {
		mainPanel.removeAll();
		createPanel(selPanel.getText());
		mainPanel.revalidate();
		//mainPanel.validate();
		//tfInput.setCaretPosition(tfInput.getText().length());
	}

	
	protected void createButtonPanel() {
		createOkButton();
		createCancelButton();
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
	}	
	
	private Vector<DocumentProfile> processFiles(String filename, String catName, boolean visualize) {
		System.out.println("Processing " + filename);
		Vector<DocumentProfile> profiles = parent.getProfiler().analyze(filename, catName, visualize);
		if(profiles.size() == 0)
			JOptionPane.showMessageDialog(this, Constants.PROFILING_FAILED, "Warning", 
					JOptionPane.WARNING_MESSAGE);		
		return profiles;
	}
	
	public DocumentProfile processText(String s){
		System.out.println("AnalyzingProcessText " + s);
		Document p = demo.getProfiler().analyze(s);
		DocumentProfile d = demo.getProfiler().analyseResults(p);
		return d;
		
		
	}
	protected Vector performTask() {		
		Vector<DocumentProfile> profiles = new Vector<DocumentProfile>();
	     setCursor(new Cursor(Cursor.WAIT_CURSOR));
	     
	     if(!parent.getProfiler().isInitialized()) {				
	    	 while(!parent.getProfiler().isInitialized()) {
	    		 Utils.pause(50);
	    	 }
	     }
	     boolean visualize = jCheckVisualize.isSelected();
	     if(jCheckProcessSubFolders.isSelected()) {
	    	File dir = new File(selPanel.getText());
	    	File[] files = dir.listFiles();
	    	for(int x = 0; x < files.length; x++) {
	    		File file = files[x];
	    		if(file.isDirectory() && !file.getName().equalsIgnoreCase("cvs")) 
	    			profiles.addAll(processFiles(file.getAbsolutePath(), file.getName(), visualize));

	    	}	    	
	     }
	     else {
	    	 profiles.addAll(processFiles(selPanel.getText(), jTextCategory.getText(), visualize));
	     }
	     //dialog.setVisible(false);

	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	     if(jCheckClassifiers.isSelected() && profiles != null)
	    	 categorize(profiles);
		return retVals;
	}
	 
}
