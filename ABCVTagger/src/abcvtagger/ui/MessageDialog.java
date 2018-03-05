package abcvtagger.ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MessageDialog extends JDialog{
	private JPanel messagePanel;
	
	public MessageDialog(JDialog parent, String message, String title) {
		super(parent, false);
		this.setAlwaysOnTop(true); 
		setTitle(title);
    	//setLayout(new BorderLayout());
    	/*add(new JPanel(), BorderLayout.NORTH);
    	add(new JPanel(), BorderLayout.SOUTH);
    	add(new JPanel(), BorderLayout.EAST);
    	add(new JPanel(), BorderLayout.WEST);*/
		setCenterPanel(message);
    	setLocation(parent.getX() + 10, parent.getY() + 10);
    }
	
	private void setCenterPanel(String message) {
    	messagePanel = new JPanel();
    	messagePanel.add(new JLabel(message));
    	getContentPane().add(messagePanel);
    	pack();		
	}
	
	public void update(String title, String message) {
		setTitle(title);
		getContentPane().remove(messagePanel);
		setCenterPanel(message);
	}

}
