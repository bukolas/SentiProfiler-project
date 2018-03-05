package censetagger.ui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import censetagger.Constants;
import censetagger.ui.profile.ProfileCreatorDialog;
import censetagger.ui.profile.ProfileManagerDialog;



/**
 * Main menubar of JittersMeter.
 * @author Tuomo Kakkonen
 *
 */
public class MenuBar extends JMenuBar  {
	private JMenu menuHelp, menuProfiler;
	private JMenuItem menuItemExit, menuItemAbout, menuCreateProfile, 
		menuItemView;
	private final MainWindow parent;

	public MenuBar(MainWindow parent) {
		this.parent = parent;
		
		menuProfiler = new JMenu("Profile");
		menuProfiler.setMnemonic(KeyEvent.VK_A);
		menuCreateProfile = new JMenuItem("Create", KeyEvent.VK_T);
		menuCreateProfile.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	//delete previously created profiles before creating new ones
		    	deletePrevProfiles();
		    	createProfile();
		    }
		});

		menuCreateProfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				ActionEvent.ALT_MASK));
		menuProfiler.add(menuCreateProfile);
		
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
		
		menuHelp = new JMenu("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuItemAbout = new JMenuItem("About ABCV Analyzer");
		menuItemAbout.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        showAboutDialog();
		    }
		});
		menuHelp.add(menuItemAbout);
		add(menuHelp);
	}
	
	protected void deletePrevProfiles() {
		// TODO Auto-generated method stub
		
		
	}

	private void showAboutDialog() {
		JOptionPane.showMessageDialog(parent, Constants.ABOUT_MESSAGE, "ABCV Analyzer", 
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void createProfile() {
		ProfileCreatorDialog profDia = new ProfileCreatorDialog(parent);
		Vector params = profDia.showDialog();
	}
		
	private void showProfiles() {
		ProfileManagerDialog pwDialog = new ProfileManagerDialog(parent);
		pwDialog.setVisible(true);
	}
		
	private void shutdown() {
		parent.shutdown();
	}

}
