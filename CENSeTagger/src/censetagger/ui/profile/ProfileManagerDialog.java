package censetagger.ui.profile;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import cense.classifier.Classifier;
import censetagger.ProfileAndOntologyManager;
import censetagger.profile.*;
import censetagger.ui.AbstractDialog;
import censetagger.ui.classify.ClassificationDialog;
import censetagger.ui.main.MainWindow;



/**
 * Dialog for viewing and comparing stored sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class ProfileManagerDialog extends AbstractDialog {
	protected ProfileTable table;
	protected JScrollPane tablePanel;
	protected MainWindow parent;
	protected JButton viewButton, compareButton, deleteButton, analyzeButton, categorizeButton;
	
	/**
	 * Crates a new instance of the dialog.
	 * @param parent Parent window of the dialog.
	 */
	public ProfileManagerDialog(MainWindow parent) {
		super("Manage, view and analyze profiles");
		//this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		update();
		this.setSize(new Dimension(775, 300)); //(650,300) when the table was smaller in ProfileTable
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	protected void createMainPanel() {
		createTable(null);	
	}
	
	/**
	 * Updates the table of sentiment profiles.
	 */
	public void update() {
		mainPanel.removeAll();
		createTable(parent.getProfiler().getProfileManager().getProfiles());
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.revalidate();
		//mainPanel.validate();
	}

	/**
	 * Creates the profile table.
	 * @param profs Vector of sentiment profiles.
	 */
	private void createTable(Vector<DocumentProfile> profs) {
		table = new ProfileTable();
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setMinimumSize(new Dimension(550, 100));
		if(profs != null)
			table.setData(profs);
		tablePanel = new JScrollPane(table);
		mainPanel.add(tablePanel);
	}
	
	/**
	 * Crates the panel with the buttons.
	 */
	protected void createButtonPanel() {
		createViewButton();		
		createCompareButton();		
		createDeleteButton();
		createAnalyzeButton();
		createCategorizeButton();
		createOkButton();				
		buttonPanel.add(new JPanel());
		
	}	
	
	/**
	 * Shows a message dialog.
	 */
	private void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message);		
	}
	
	/**
	 * Displays a dilaog for confirming deletion of profile(s).
	 * @return
	 */
	private int confirmDelete() {
		return JOptionPane.showConfirmDialog(this, "Do you really want to remove the selected profile(s)?");		
	}
	
	/**
	 * Creates the viewing button.
	 */
	protected void createViewButton() {
		viewButton = new JButton("View");
		viewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<DocumentProfile> profs = table.getSelectedProfiles();
				if(profs.size() != 1) 
					showMessage("Select one profile to view.");
				//else 
					//profs.get(0).visualize(profs.get(0).getName());
		       }
			}
		);  
		buttonPanel.add(viewButton);
	}
	
	
	/**
	 * Creates the categorize button.
	 */
	protected void createCategorizeButton() {
		categorizeButton = new JButton("Categorize");
		categorizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<DocumentProfile> profs = table.getSelectedProfiles();
				ClassificationDialog cd = new ClassificationDialog(parent, profs);
				cd.showDialog();
			}
		}
		);  
		buttonPanel.add(categorizeButton);
	}
	
	/**
	 * Creates the comparison button.
	 */
	protected void createCompareButton() {
		compareButton = new JButton("Compare");
		compareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				Vector<DocumentProfile> profs = table.getSelectedProfiles();
				if(profs.size() != 2)
					showMessage("Select two profiles to compare.");
				else 		
					parent.getProfiler().compareResults(0, profs.get(0), profs.get(1));		
			 	}
			}
		);  
		buttonPanel.add(compareButton);
	}

	/**
	 * Creates the analysis button.
	 */
	protected void createAnalyzeButton() {
		analyzeButton = new JButton("Analyze");
		analyzeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				Vector<DocumentProfile> profs = table.getSelectedProfiles();
				if(profs.size() < 2)
					showMessage("Select at lesat two profiles to analyze.");
				else {					
					ProfileComparator pc = new ProfileComparator(profs);
					pc.analyze();
				}
			  }
			}
		);  
		buttonPanel.add(analyzeButton);
	}

	
	/**
	 * Creates the delete button.
	 */
	protected void createDeleteButton() {
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				Vector<DocumentProfile> profs = table.getSelectedProfiles();
				int n = confirmDelete();
				if(n == JOptionPane.YES_OPTION) {
					for(DocumentProfile p : profs)
						parent.getProfiler().getProfileManager().deleteProfile(p);
						update();
				}
		       }
			}
		);  
		buttonPanel.add(deleteButton);
	}		
	
	protected Vector performTask() { 
		retVals = new Vector();
		return retVals;
	}
	 
}
