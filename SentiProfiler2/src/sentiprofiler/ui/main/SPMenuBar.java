package sentiprofiler.ui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import sentiprofiler.Constants;
import sentiprofiler.ui.ontology.OntologyCreatorDialog;
import sentiprofiler.ui.profile.ProfileManagerDialog;
import sentiprofiler.ui.profile.ProfileComparatorDialog;
import sentiprofiler.ui.profile.SPProfileCreatorDialog;

/**
 * Main menubar of JittersMeter.
 * @author Tuomo Kakkonen
 *
 */
public class SPMenuBar extends JMenuBar  {
	private JMenu menuOntology, menuHelp, menuProfiler;
	private JMenuItem menuItemCreate, menuItemExit, menuItemAbout, menuCreateProfile, 
		menuItemCompare, menuItemView;
	private final SPMainWindow parent;

	public SPMenuBar(SPMainWindow parent) {
		this.parent = parent;
		
		menuProfiler = new JMenu("Profile");
		menuProfiler.setMnemonic(KeyEvent.VK_A);
		menuCreateProfile = new JMenuItem("Create", KeyEvent.VK_T);
		menuCreateProfile.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	createProfile();
		    }
		});

		menuCreateProfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				ActionEvent.ALT_MASK));
		menuProfiler.add(menuCreateProfile);
		menuItemCompare = new JMenuItem("Create and compare", KeyEvent.VK_T);
		menuItemCompare.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				ActionEvent.ALT_MASK));
		menuItemCompare.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	compareProfiles();
		    }
		});
		menuProfiler.add(menuItemCompare);
		
		menuProfiler.addSeparator();
		
		menuItemView = new JMenuItem("View all", KeyEvent.VK_A);
		menuItemView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.ALT_MASK));
		menuItemView.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	showProfiles();
		    }
		});
		menuProfiler.add(menuItemView);
		menuProfiler.addSeparator();
		
		menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	shutdown();
		    }
		});
		menuProfiler.add(menuItemExit);

		
		add(menuProfiler);

		
		menuOntology = new JMenu("Ontology");
		menuOntology.setMnemonic(KeyEvent.VK_A);
		menuItemCreate = new JMenuItem("Create", KeyEvent.VK_O);
		menuItemCreate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.ALT_MASK));
		menuItemCreate.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	createOntology();
		    }
		});

		menuOntology.add(menuItemCreate);
		add(menuOntology);
		
		menuHelp = new JMenu("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuItemAbout = new JMenuItem("About SentiProfiler");
		menuItemAbout.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        showAboutDialog();
		    }
		});
		menuHelp.add(menuItemAbout);
		add(menuHelp);
	}
	
	private void showAboutDialog() {
		JOptionPane.showMessageDialog(parent, Constants.ABOUT_MESSAGE, "SentiProfiler", 
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void createProfile() {
		SPProfileCreatorDialog profDia = new SPProfileCreatorDialog(parent);
		Vector params = profDia.showDialog();
	}
	
	private void compareProfiles() {
		ProfileComparatorDialog profDia = new ProfileComparatorDialog(parent);
		Vector params = profDia.showDialog();
	}
	
	private void showProfiles() {
		ProfileManagerDialog pwDialog = new ProfileManagerDialog(parent);
		pwDialog.setVisible(true);
	}
	
	private void createOntology() {
		OntologyCreatorDialog ontDialog = new OntologyCreatorDialog(parent);
		ontDialog.setVisible(true);
	}
	
	private void shutdown() {
		parent.shutdown();
	}

}
