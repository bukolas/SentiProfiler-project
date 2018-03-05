package abcvtagger.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import abcvtagger.utils.TextFileFilter;
import abcvtagger.utils.Utils;


public class FileSelectionPanel extends JPanel {
	protected JTextField tf = new JTextField();
	protected JButton button;
	
	public FileSelectionPanel(final AbstractDialog parent, String label, final String dir) {						
		tf = new JTextField(dir);
		
		button = new JButton("Choose");
		button.addActionListener(new ActionListener () {		
			public void actionPerformed(ActionEvent e) {
	            String fn = Utils.chooseFile(button, 
	            		dir, new TextFileFilter(), JFileChooser.FILES_AND_DIRECTORIES);
	            if(fn.length() > 0) {
	            	tf.setText(fn);
	    			parent.update();
	            }                	
			}
		}
		);  
		tf.setColumns(40);
		add(new JLabel(label));
		add(tf);
		add(button);				
	}
	
	public String getText() {
		return tf.getText();
	}
}
