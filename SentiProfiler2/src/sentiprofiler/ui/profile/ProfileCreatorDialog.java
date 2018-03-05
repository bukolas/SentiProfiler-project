package sentiprofiler.ui.profile;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sentiprofiler.Constants;
import sentiprofiler.ui.AbstractDialog;
import sentiprofiler.utils.RDFFileFilter;
import sentiprofiler.utils.TextFileFilter;
import sentiprofiler.utils.Utils;

/**
 * Dialog for creating sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class ProfileCreatorDialog extends AbstractDialog {
	
	public ProfileCreatorDialog(Component parent) {
		super("Create sentiment profile");
		setLocation(parent.getX() + 10, parent.getX() + 10);
	}
	
	public void update() {
		;
	}
	
	protected void createMainPanel() {
		final String curDir = Utils.getCurrentDirectory() + File.separator;
		
		JPanel inputFilePanel = new JPanel();
		JLabel lab = new JLabel("Input file");	
		final JTextField tfInput = new JTextField(curDir);
		final JButton bChooseInput = new JButton("Choose");
		bChooseInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				String selFile  = Utils.chooseFile(bChooseInput, curDir, new TextFileFilter());
				if(selFile.length() > 0)
					tfInput.setText(selFile);
			}
		}
		);  
		tfInput.setColumns(40);
		inputFilePanel.add(lab);
		inputFilePanel.add(tfInput);
		inputFilePanel.add(bChooseInput);
		
		
		
		JPanel ontologyPanel = new JPanel();
		JLabel lab2 = new JLabel("Ontology");	
		final JTextField tfOntFile = new JTextField(curDir + Constants.ONTOLOGY_DIR);
		final JButton bChooseOnt = new JButton("Choose");
		bChooseOnt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				String selFile = Utils.chooseFile(bChooseOnt, curDir + Constants.ONTOLOGY_DIR, new RDFFileFilter());
				if(selFile.length() > 0) 
					tfOntFile.setText(selFile);
			}
			}
		);  
		tfOntFile.setColumns(40);
		ontologyPanel.add(lab2);
		ontologyPanel.add(tfOntFile);
		ontologyPanel.add(bChooseOnt);
		
		JPanel hierarchyPanel = new JPanel();
		JLabel lab3 = new JLabel("Hierarchy");	
		final JTextField tfHierarchyFile = new JTextField(curDir + Constants.WN_AFFECT_DIR);
		final JButton bChooseHierarchy = new JButton("Choose");
		bChooseHierarchy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				String selFile = Utils.chooseFile(bChooseHierarchy, curDir + Constants.WN_AFFECT_DIR, null);
				if(selFile.length() > 0) 
					tfHierarchyFile.setText(selFile);
			}
			}
		);  
		tfHierarchyFile.setColumns(40);
		hierarchyPanel.add(lab3);
		hierarchyPanel.add(tfHierarchyFile);
		hierarchyPanel.add(bChooseHierarchy);
		
		JPanel filePanel = new JPanel(new GridLayout(3,0));
		filePanel.add(inputFilePanel);
		filePanel.add(ontologyPanel);
		filePanel.add(hierarchyPanel);
		mainPanel.add(filePanel);
	}
	
	protected void createButtonPanel() {
		createOkButton();
		createCancelButton();
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
	}	
	
	protected Vector performTask() {
		return retVals;
	}
}
