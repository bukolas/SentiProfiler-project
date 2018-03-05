package MainCode;

import gate.util.GateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.plaf.basic.BasicBorders.RadioButtonBorder;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import MainCode.StandAloneAnnie;

public class MainWindow {

	protected Shell shell;
	private Text txtInput;
	private Text txtOutput;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(600, 491);
		shell.setText("The Love Letter Project ver 1.0 Beta");
		
		Label label = new Label(shell, SWT.NONE);
		label.setText("Input text");
		label.setBounds(10, 10, 55, 15);
		
		txtInput = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		txtInput.setBounds(10, 37, 424, 175);
		
		Label label_1 = new Label(shell, SWT.NONE);
		label_1.setText("Output text");
		label_1.setBounds(10, 243, 73, 15);
		
		txtOutput = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtOutput.setEditable(false);
		txtOutput.setBounds(10, 266, 424, 175);
		
		Group group = new Group(shell, SWT.NONE);
		group.setText("Translation Types");
		group.setBounds(456, 37, 118, 128);
		
		Button btnLoveLetter = new Button(group, SWT.RADIO);
		btnLoveLetter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StandAloneAnnie.emotionState="Love";
			}
		});
		btnLoveLetter.setText("Love Letter");
		btnLoveLetter.setBounds(10, 26, 90, 16);
		
		Button btnFriendlyLetter = new Button(group, SWT.RADIO);
		btnFriendlyLetter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StandAloneAnnie.emotionState="Friendly";
			}
		});
		//button_1.setEnabled(false);
		btnFriendlyLetter.setText("Friendly Letter");
		btnFriendlyLetter.setBounds(10, 62, 98, 16);
		
		Button btnFormalLetter = new Button(group, SWT.RADIO);
		btnFormalLetter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StandAloneAnnie.emotionState="Formal";
			}
		});
		btnFormalLetter.setText("Formal Letter");
		btnFormalLetter.setBounds(10, 98, 90, 16);
		
		final Button btnSave = new Button(shell, SWT.NONE);
		btnSave.setEnabled(false);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog=new FileDialog(shell,SWT.SAVE);
				dialog.setFilterNames(new String[] { "Text Files (*.txt)" });
				dialog.setFilterExtensions(new String[] { "*.txt" });
				String filterPath = "/";
				String platform = SWT.getPlatform();
				if (platform.equals("win32") || platform.equals("wpf")) {
					filterPath = "c:\\";
				}
				String fn = dialog.open();
				if (fn != null) {
					File file = new File(fn);

					FileWriter fileWriter;
					try {
						fileWriter = new FileWriter(file);
						fileWriter.write(txtOutput.getText());
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
			}
		});
		btnSave.setBounds(469, 243, 85, 34);
		btnSave.setText("Save");
		
		Button button_3 = new Button(shell, SWT.NONE);
		button_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if((txtInput.getText().trim().length()>0)&&(StandAloneAnnie.emotionState.trim().length()>0))
				{
					
					try{
					 
					 FileWriter writer2 = new FileWriter("textInput.txt");
				     writer2.write(txtInput.getText());
				     writer2.close();
				     String inputAr[]={"file:textInput.txt"};
				     try{
				     StandAloneAnnie.main(inputAr);
				     
				     
				     txtOutput.setText(StandAloneAnnie.outputText);
				     btnSave.setEnabled(true);
				     }
				     catch(GateException ex)
				     {
				    	 
				     }
					}
					 catch (IOException ioe)
				      {
				      ioe.printStackTrace();
				      }
				}
			}
		});
		button_3.setText("Convert");
		button_3.setBounds(469, 185, 85, 34);
		
		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				 shell.dispose();
				    System.exit(0);
			}
		});
		btnExit.setBounds(469, 407, 85, 34);
		btnExit.setText("Exit");
		
		

	}
}
