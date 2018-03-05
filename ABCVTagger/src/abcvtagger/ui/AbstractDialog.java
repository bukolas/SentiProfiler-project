package abcvtagger.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Abstract dialog class that provides the basic 
 * dialog functionality and layout. 
 * @author Tuomo Kakkonen
 */
public abstract class AbstractDialog extends JDialog {
	protected JPanel mainPanel, buttonPanel;
	protected JButton okButton, cancelButton;
	protected Vector retVals;
	
	/**
	 * Creates an instance of the class and sets the basic
	 * panels and buttons.
	 * @param title Title of the dialog.
	 */
	public AbstractDialog(String title) {
		setTitle(title);
		setLayout(new BorderLayout());
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.WEST);
		add(new JPanel(), BorderLayout.EAST);
		
		mainPanel = new JPanel(new BorderLayout());
		createMainPanel();		
		add(mainPanel, BorderLayout.CENTER);
		
		buttonPanel = new JPanel();
		createButtonPanel();
		add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}
	
	protected abstract void createMainPanel();	
	protected abstract void createButtonPanel();
	/**
	 * Specifies the task to be performed when the OK button is clicked.
	 * @return Vector return values from the task.
	 */
	protected abstract Vector performTask();
	public abstract void update();
	
	/**
	 * Creates the OK button.
	 */
	protected void createOkButton() {
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
				retVals = performTask();
		        if(retVals != null)
		        	setVisible(false);
		       }
			}
		);  
		buttonPanel.add(okButton);
	}

	/**
	 * Creates the cancel button that closes the dialog.
	 */
	protected void createCancelButton() {
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
		        	close();
		       }
			}
		);  
		buttonPanel.add(cancelButton);
	}

	
	private void close() {
		setVisible(false);
	}
	
	public Vector showDialog() {
		setVisible(true);
		return retVals;
	}
	
}
