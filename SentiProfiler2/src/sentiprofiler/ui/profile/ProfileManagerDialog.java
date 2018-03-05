package sentiprofiler.ui.profile;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

//import abcvtagger.profile.DocumentProfile;
//import abcvtagger.profile.ProfileComparator;
//import abcvtagger.ui.classify.ClassificationDialog;
import sentiprofiler.profile.SentimentProfile;
import sentiprofiler.ui.AbstractDialog;
import sentiprofiler.ui.main.SPMainWindow;
import sentiprofiler.profile.ProfileComparator;

/**
 * Dialog for viewing and comparing stored sentiment profiles.
 * @authors Tuomo Kakkonen
 *         Calkin Suero Montero
 */
public class ProfileManagerDialog extends AbstractDialog {
	protected ProfileTable table;
	protected JScrollPane tablePanel;
	protected SPMainWindow parent;
	protected JButton viewButton, compareButton, deleteButton, analyzeButton, categorizeButton;
	
	/**
	 * Crates a new instance of the dialog.
	 * @param parent Parent window of the dialog.
	 */
	public ProfileManagerDialog(SPMainWindow parent) {
		super("Manage and view sentiment profiles");
		//this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		update();
		this.setSize(new Dimension(900, 300));
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
	}

	/**
	 * Creates the profile table.
	 * @param profs Vector of sentiment profiles.
	 */
	private void createTable(Vector<SentimentProfile> profs) {
		table = new ProfileTable();
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setMinimumSize(new Dimension(500, 100));
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
		//buttonPanel.add(viewButton);
		createCompareButton();		
		//buttonPanel.add(compareButton);
		createAnalyzeButton();
		//buttonPanel.add(analyzeButton);
//		createCategorizeButton();
//		buttonPanel.add(categorizeButton);
		createDeleteButton();
		//buttonPanel.add(deleteButton);
		createOkButton();		
		//buttonPanel.add(okButton);
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
				Vector<SentimentProfile> profs = table.getSelectedProfiles();
				if(profs.size() < 1) 
					showMessage("Select at least one profile to view.");
				else {
					for (SentimentProfile p: profs)
					//profs.get(0).visualize(profs.get(0).getName());
							p.visualize(p.getName());
				}
		       }
			}
		);  
		buttonPanel.add(viewButton);
	}

	/**
	 * Creates the comparison button.
	 */
	protected void createCompareButton() {
		compareButton = new JButton("Compare");
		compareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				Vector<SentimentProfile> profs = table.getSelectedProfiles();
				if(profs.size() != 2)
					showMessage("Select two profiles to compare.");
				else {					
					if(profs.get(0).getOntology().getId() != profs.get(1).getOntology().getId())
						showMessage("Cannot compare profiles that are based on different ontologies.");
					else {
						parent.getProfiler().compareResults(0, profs.get(0), profs.get(1));
						profs.get(0).visualizeComparison(profs.get(0).getName());
						profs.get(1).visualizeComparison(profs.get(1).getName());
						//profs.get(0).visualize(profs.get(0).getName());
						//profs.get(1).visualize(profs.get(1).getName());
						//parent.getProfiler().compareResults(0, profs.get(0), profs.get(1));
					}
				}
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
				Vector<SentimentProfile> profs = table.getSelectedProfiles();
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
	 * Creates the categorization button.
	 */
	/*protected void createCategorizeButton() {
		categorizeButton = new JButton("Categorize");
		//categorizeButton.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		ClassificationDialog cd = new ClassificationDialog(parent);
		//		cd.showDialog();
		//	}
		//}
		//);  
		buttonPanel.add(categorizeButton);
	}
	*/
	
	/**
	 * Creates the delete button.
	 */
	protected void createDeleteButton() {
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				Vector<SentimentProfile> profs = table.getSelectedProfiles();
				int n = confirmDelete();
				System.out.println("confirmation: " +n + "all profiles: " + profs.size());
				if(n == JOptionPane.YES_OPTION) {
					for(SentimentProfile p : profs){
						System.out.println("deleting profiles: " + p.getName() + " of " +profs.size());
						parent.getProfiler().getProfileManager().deleteProfile(p);
						update();	
					}
					
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
