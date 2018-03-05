package circumplex;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;


/** 
 * MainWindow.java - Displays the annotation interface, retrieves and displays each sentence from a file.
 * Allows you to save and load annotation states.
 * Allows you to export the results file with emotion intensity and intensity
 *   
 * @version 1.0 - 2011
 * */


public class MainWindow extends JFrame implements ActionListener, WindowListener {	
	private static DataManager dmanager;
	private static CircumplexComponent cccomponent;
	private int index = 0;
	private Vector<String> sentences;
	private Properties properties;		
	boolean disableBack = false; // If you do not want to the previous button	
	boolean win = false;	
	private JTextPane inDataBox;
	private JButton nextButton, prevButton, startButton, finishButton, gotoButton, definitionButton, deleteButton;
	//private JLabel sentence;
	private JTextField indexText;
	private File file, prevFile, savefile, savedFilename;
	boolean started, prevfilestate;	
	private JPanel buttonPanel, dataPanel;
	private String inputFile;
	private JMenuBar menuBar;
	private JMenuItem loadMenuItem, saveMenuItem, saveAsMenuItem,loadStateMenuItem;
	boolean savestate = false;	
	private String inputname = "", savedname = "";
	
	/** 
	 * MainWindow displays the sentences, annotation component, 
	 * transition buttons and File menu.
	 * 
	 */
	public MainWindow() {		
		setTitle("Annotation tool for cicrumplex model of emotions");
		sentences = new Vector<String>();		
		cccomponent = new CircumplexComponent();		
		//dmanager = new DataManager("data/tests/round2/test_round2.txt");		
		inDataBox = new JTextPane();		
		
		properties = new Properties();
		String path = "../prevFile.properties";
		prevFile = new File(path);
		inputFile = new String();
		started = false; 
		prevfilestate = false;
		
		//String fname = getFilename();
		//dmanager = new DataManager(fname);
		//sentences = dmanager.getSentences(); // get sentences from the sentence input file	
		
		//inDataBox.setHorizontalTextPosition(SwingConstants.LEFT);
		//index+1 + ". " + data[index]
		inDataBox.setText("Click the Start button to start annotating. With each sentence, click and drag your mouse to choose Emotion Quality and Intensity");
		inDataBox.setPreferredSize(new Dimension(800,124));
		inDataBox.setFont(new Font( "Courier", Font.BOLD, 22 ));
		
		createMenuBar();
		
		// add annotation component		
		cccomponent.setMinimumSize(new Dimension(0,500));
		cccomponent.setMaximumSize(new Dimension(0,500));
		cccomponent.setPreferredSize(new Dimension(0,500));
		cccomponent.setEnabled(false);
		
		
		createButtonPanel();		
		createDataPanel();
		
			
		this.setJMenuBar(menuBar);		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);	
		//this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
			
		this.add(dataPanel);
		this.pack();
		this.setVisible(true);
		
	}

	private void createDataPanel() {
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
		dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		dataPanel.add(inDataBox);
		dataPanel.add(Box.createRigidArea(new Dimension(10,0)));
		dataPanel.add(cccomponent);
		dataPanel.add(Box.createRigidArea(new Dimension(10,0)));
		dataPanel.add(buttonPanel);
	}

	private void createButtonPanel() {
		// Bottom buttons panel
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// add buttons with constraints
		prevButton = new JButton("Previous");
		prevButton.addActionListener(this);
		prevButton.setVisible(true);
		prevButton.setEnabled(false);
		prevButton.setPreferredSize(new Dimension(120, 32));
		prevButton.setMinimumSize(new Dimension(120, 32));
		prevButton.setMaximumSize(new Dimension(120, 32));
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.45f;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		buttonPanel.add(prevButton, c);
		
		startButton = new JButton("Start");
		startButton.addActionListener(this);
		startButton.setPreferredSize(new Dimension(120, 32));
		startButton.setMinimumSize(new Dimension(120, 32));
		startButton.setMaximumSize(new Dimension(120, 32));
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.45f;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 2;
		buttonPanel.add(startButton, c);
		
		nextButton = new JButton("Next");
		nextButton.addActionListener(this);
		nextButton.setPreferredSize(new Dimension(120, 32));
		nextButton.setMinimumSize(new Dimension(120, 32));
		nextButton.setMaximumSize(new Dimension(120, 32));
		nextButton.setVisible(false);
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.45f;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 2;
		buttonPanel.add(nextButton, c);		
	
		finishButton = new JButton("Finish");
		finishButton.addActionListener(this);
		finishButton.setVisible(false);
		finishButton.setPreferredSize(new Dimension(120, 32));
		finishButton.setMinimumSize(new Dimension(120, 32));
		finishButton.setMaximumSize(new Dimension(120, 32));
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.45f;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 2;
		buttonPanel.add(finishButton, c);
		
		definitionButton = new JButton("Show definition");
		definitionButton.addActionListener(this);
		definitionButton.setVisible(true);		
		c.fill = GridBagConstraints.CENTER;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0.05f;
		c.gridx = 2;
		c.gridy = 0;
		buttonPanel.add(definitionButton, c);
		
		deleteButton = new JButton("Delete Word");
		deleteButton.addActionListener(this);
		deleteButton.setVisible(true);		
		c.fill = GridBagConstraints.CENTER;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.05f;
		c.gridx = 4;
		c.gridy = 0;
		buttonPanel.add(deleteButton, c);
		
		//add text box
		indexText = new JTextField(10);
		indexText.setPreferredSize(new Dimension(60, 32));
		indexText.setMinimumSize(new Dimension(60, 32));
		indexText.setMaximumSize(new Dimension(60, 32));		
		indexText.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.05f;
		//c.gridwidth = 60;
		c.gridx = 2;
		c.gridy = 1;
		buttonPanel.add(indexText, c);
		
		// add Go To button
		gotoButton = new JButton ("Go To");
		gotoButton.addActionListener(this);
		gotoButton.setVisible(true);
		gotoButton.setPreferredSize(new Dimension(78, 32));
		gotoButton.setMinimumSize(new Dimension(78, 32));
		gotoButton.setMaximumSize(new Dimension(78, 32));	
		gotoButton.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.05f;
		//c.gridwidth = 78;
		c.gridx = 4;
		c.gridy = 1;
		buttonPanel.add(gotoButton, c);
		}

	private void createMenuBar() {
		menuBar = new JMenuBar();
		
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		
		loadMenuItem = new JMenuItem("Load Input file", KeyEvent.VK_I);
		loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
		loadMenuItem.getAccessibleContext().setAccessibleDescription("This chooses the input file to annotate");
		loadMenuItem.addActionListener(this);
		menu.add(loadMenuItem);
		
		saveMenuItem = new JMenuItem("Save state", KeyEvent.VK_S);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		saveMenuItem.getAccessibleContext().setAccessibleDescription("This saves the annotation state to a file");
		saveMenuItem.addActionListener(this);
		menu.add(saveMenuItem);
		
		saveAsMenuItem = new JMenuItem("Save state as");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		saveAsMenuItem.getAccessibleContext().setAccessibleDescription("Choose the file to save the annotation state");
		saveAsMenuItem.addActionListener(this);
		menu.add(saveAsMenuItem);
		
		loadStateMenuItem = new JMenuItem("Load state", KeyEvent.VK_L);
		loadStateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
		loadStateMenuItem.getAccessibleContext().setAccessibleDescription("This loads the experiment state from a file");
		loadStateMenuItem.addActionListener(this);
		menu.add(loadStateMenuItem);
		
//		menuItem = new JMenuItem("Export results", KeyEvent.VK_E);
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
//		menuItem.getAccessibleContext().setAccessibleDescription("This exports the experiment results to a file");
//		menuItem.addActionListener(this);
//		menu.add(menuItem);
//		

	}

	/** Specifies the actions when Start, Next, Previous, Finish, Go To buttons and File Menu options are clicked
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {		
		System.out.println(e.getActionCommand());		
		//dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());
		//dmanager.save();

		if(("Show definition").equals(e.getActionCommand())){
			String word = sentences.get(index);
			BrowserLauncher.openURL("http://www.thefreedictionary.com/" + word);
		}
		else if (("Start").equals(e.getActionCommand())){
			index = 0;		
			loadPrevFile();
			//angleLabel();
			dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());
			loadMenuItem.setEnabled(false);
			gotoButton.setEnabled(true);
			indexText.setEnabled(true);
		}
		else if (("Go To").equals(e.getActionCommand())) {
			dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());						
			//get the current index
			int i=index;
			boolean match = false;

			String indextxt = indexText.getText(); // text from textbox
			indextxt = indextxt.toLowerCase();
			/*// getWordIndex(indextxt)and compare it to sentences vector loop
			for (int j=0; j < sentences.size(); j++)
			{
				String holder = sentences.elementAt(j);
				if (indextxt.equals(holder))
				{
					i = j;
					match = true;
				}
			}*/

			try {
				i = Integer.parseInt(indextxt);
					if(i <= 0){
		    			i=1;
		    			return; }
		    		--i;
		    		if(i >= sentences.size()) return;
		    		System.out.println("Number:  " + i);
			} catch (NumberFormatException e2) {
				// getWordIndex(indextxt)and compare it to sentences vector loop
				for (int j=0; j < sentences.size(); j++)
				{
				String holder = sentences.elementAt(j);
					if (indextxt.equals(holder))
					{
						i = j;
						match = true;
					}
				}
				System.out.println("String:  " + i);
			}
			/*
		    if(i <= 0){
		    	i=1;
		    	return;
		    }*/
		   // --i;
		  //  if(i >= sentences.size()) return;
		    index = i;
			inDataBox.setText(index + ". " + sentences.get(index));
		}		
		else if (("Delete Word").equals(e.getActionCommand())) {
			dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());			

			if (savestate){ //if file has been saved		
			
			int ind = index;
			int delete = JOptionPane.showConfirmDialog(this, "Do you want to delete '" +sentences.get(ind)+"'" +"?", "Circumplex Annotation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (delete == JOptionPane.NO_OPTION){
				return;
			}else{
				dmanager.deleteDataFor(sentences.get(ind));
				sentences.remove(ind);
				JOptionPane.showMessageDialog(this, "Word has been deleted", "Circumplex Annotation", JOptionPane.INFORMATION_MESSAGE);
				ind++;
				inDataBox.setText(ind+1 + ". " + sentences.get(ind));		
				cccomponent.setData(dmanager.getDataFor(sentences.get(ind)));		
					
				//update results, increase index
			}
			}else {
				JOptionPane.showMessageDialog(this, "Please save your file first before deleting", "Circumplex Annotation", JOptionPane.INFORMATION_MESSAGE);

			}
		}
		else if(("Next").equals(e.getActionCommand())){
			dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());
			indexText.setText("");

			index++;
			if(index >= sentences.size())
				index = sentences.size() - 1;			
		} 
		else if(("Previous").equals(e.getActionCommand())){
			dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());			
			indexText.setText("");			
			index--;
			if(index < 1)
				index = 0;			
		} 
		else if (("Save state as").equals(e.getActionCommand())){
			//Show dialog to save file with annotations
			JFileChooser FC = new JFileChooser("."); 
			FC.setDialogTitle("Save state");
			int saveChoice = FC.showSaveDialog(this);
			
			if (saveChoice == JFileChooser.APPROVE_OPTION)
			{
			//get fileName
				file = FC.getSelectedFile();	
				saveState(file);	
				dmanager.saveToFile(file.getAbsolutePath());
				savedFilename = file;
				savestate = true;
				savedname = file.getName();
				setToolTitle(inputname, savedname);
				
			}
			else return;			
		} 
		else if (("Save state").equals(e.getActionCommand())){
			//if new new file and click on save, then do the same as save as
				if (savestate){
					File f = savedFilename;
					//get absolute path of savedFilename
					//System.out.println("inside savestate: ");
					String absolutepath = f.getAbsolutePath();
					String ch = File.separator;
					int p = absolutepath.lastIndexOf(ch);
					String absolute = absolutepath.substring(0, p+1);
					//System.out.println("absolute path " + absolutepath);
					saveState(f);	
					String name = f.getName();
					if (f.getName().contains(".stt"))
					{
						//remove the .stt
						
						int n = f.getName().lastIndexOf(".stt");
						name = name.substring(0,n) ;
						
						absolutepath = absolute.concat(name);
						System.out.println("save state name" + absolutepath);
						dmanager.saveToFile(absolutepath);
						//System.out.println("inside loadinputfile2:"+absolutepath);
					}
					    dmanager.saveToFile(absolutepath);
					}
				
				else {
					actionPerformed(new ActionEvent(this, 0, "Save state as"));
					}
					
				}
			
			//if from a loaded state, then get name of file.									
		
		else if (("Load state").equals(e.getActionCommand())){
					loadState();
					loadMenuItem.setEnabled(false);
					gotoButton.setEnabled(true);
					indexText.setEnabled(true);
		}



		else if (("Load Input file").equals(e.getActionCommand())){

			loadInputFile();
			loadMenuItem.setEnabled(false);
			gotoButton.setEnabled(true);
			indexText.setEnabled(true);		
			}
			
//							else if (("Export results").equals(e.getActionCommand())){
//			//Show dialog to save file with annotations
//			JFileChooser FC = new JFileChooser("."); 
//			int saveChoice = FC.showSaveDialog(this);
//			
//			if (saveChoice == JFileChooser.APPROVE_OPTION)
//			{
//			//get fileName
//				file = FC.getSelectedFile();
//				
//				dmanager.saveToFile(file.getAbsolutePath());
//								
//			}
//			
//		} 
		else if(("Finish").equals(e.getActionCommand())){
			dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());
			cccomponent.setVisible(false);
			inDataBox.setText("You are done :)");
			buttonPanel.setVisible(false);
			actionPerformed(new ActionEvent(this, 0, "Save state"));
			JOptionPane.showMessageDialog(this, "You are Finished! Your work has been saved", "Circumplex Annotation", JOptionPane.INFORMATION_MESSAGE);
			this.dispose();
		}
		
		if(index == 0)
			prevButton.setEnabled(false); 
		else if(!disableBack){
			prevButton.setEnabled(true);			
		}

		if(index == sentences.size()-1){
			nextButton.setVisible(false);
			//saveButton.setVisible(false);
			finishButton.setVisible(true);
		} else {
			nextButton.setVisible(true);
			//saveButton.setVisible(true);
			finishButton.setVisible(false);
		} 
		
		if(started)
			startButton.setVisible(true);	
		
		else 
			startButton.setVisible(false);
		
		
		 inDataBox.setText(index+1 + ". " + sentences.get(index));		
		cccomponent.setData(dmanager.getDataFor(sentences.get(index)));		
		
	}
 
	
	/** Runs the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MainWindow main = new MainWindow();
		main.setVisible(true);
	}
	
	/** Saves data from DataManager into a file
	 * 
	 * @param file - File data type
	 */
	private void saveState(File file){		
		dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());
		dmanager.storePage(index);
		
		try {
			FileOutputStream fos;
			
			if (file.getName().contains(".stt"))
			{
				fos = new FileOutputStream(file);
			}else {
				fos = new FileOutputStream(file+".stt");
			}
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(dmanager.getData());
			oos.close();
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/** 
	 * Retrieves data from previously saved file.
	 * Allows to continue annotation from where left off
	 */	
	private void loadState(){		
		File stateFile;
		//Show dialog to load file with annotations
		JFileChooser FC = new JFileChooser("."); 
		FC.setDialogTitle("Choose state file");
		int choise = FC.showOpenDialog(this);
		
		if (choise == JFileChooser.APPROVE_OPTION) {
			stateFile = FC.getSelectedFile();	
		} else return;
		
		try {
			FileInputStream fis = new FileInputStream(stateFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
            dmanager = new DataManager(stateFile.getAbsolutePath());
			dmanager.setData((Data) ois.readObject());
			index = dmanager.retrievePage();
			started = false;
			savestate = true;
			savedFilename = stateFile;
			startButton.setVisible(false);
			sentences = dmanager.getSentences();
			inDataBox.setText(index+1 + ". " + sentences.get(index));
			cccomponent.setData(dmanager.getDataFor(sentences.get(index)));
			ois.close();
			savedname = stateFile.getName();
			setToolTitle(inputFile, savedname);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void loadPrevFile() 	{
		if (prevFile.exists()){		
		//String filename = new String();
		try {
			properties.load(new FileInputStream(prevFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(properties.isEmpty()) // if property is empty then setproperty
		{	
			loadInputFile();
			/*try{
			//Show dialog to choose file for annotation
			JFileChooser FC = new JFileChooser("."); 
			int choise = FC.showOpenDialog(this);
			if (choise == JFileChooser.APPROVE_OPTION) {
			filename = FC.getSelectedFile().toString();	
			
			
			properties.setProperty("previousfile", filename);
			properties.store(new FileOutputStream(prevFile), null);
			//setFilename(filename);
			dmanager = new DataManager(filename);
			sentences = dmanager.getSentences(); // get sentences from the sentence input file	
			//prevfilestate = true;
			inputname = filename;
			setToolTitle(inputname, savedname);
			System.out.println("chosen filename: "+filename + "and" + inputFile);
			}else return;
			
			} catch (IOException e){
				e.printStackTrace();
			}*/
		}
			
		else { //open previous one			
		
					//load properties file
		
			System.out.println("name from properties: " +properties.getProperty("previousfile"));
			//setFilename(filename);
		 
			dmanager = new DataManager(properties.getProperty("previousfile"));	
			sentences = dmanager.getSentences(); // get sentences from the sentence input file	
			inputname = properties.getProperty("previousfile");
			String ch = File.separator;
			int i = inputname.lastIndexOf(ch);
			inputname = inputname.substring(i+1);
			setToolTitle(inputname, savedname);
			//prevfilestate = true;
		}
		}
		else
			loadInputFile();
		}
		
		
	public void loadInputFile()	{
		String inputFilename = new String();
		//System.out.println("inside loadinputfile: ");
		try{
			//Show dialog to choose file for annotation
			System.out.println("inside loadinputfile: ");
			JFileChooser FC = new JFileChooser("."); 
			FC.setDialogTitle("Choose the input file");
			int choise = FC.showOpenDialog(this);
			if (choise == JFileChooser.APPROVE_OPTION) {
			inputFilename = FC.getSelectedFile().toString();	
			}else return;
			
			properties.setProperty("previousfile", inputFilename);
			properties.store(new FileOutputStream(prevFile), null);
			//setFilename(filename);
			dmanager = new DataManager(inputFilename);
			sentences = dmanager.getSentences(); // get sentences from the sentence input file	
			inputname = inputFilename;
			String ch = File.separator;
			int i = inputname.lastIndexOf(ch);
			inputname = inputname.substring(i+1);
			setToolTitle(inputname, savedname);

			//prevfilestate = true;
			System.out.println("loaded filename: "+inputFilename);
			
			
			} catch (IOException e){
				e.printStackTrace();
			}
		
	}
	
	public void setToolTitle (String input, String saved) {
		if (!input.isEmpty()& (!saved.isEmpty())){
			this.setTitle("Annotation tool for cicrumplex model of emotions "+ "(" + input+ ")" + " - " + saved);
		}
		else if (input.isEmpty()){
			this.setTitle("Annotation tool for cicrumplex model of emotions " + " - " + saved);
		}
		else if (saved.isEmpty()){
			this.setTitle("Annotation tool for cicrumplex model of emotions "+ "(" + input + ")");
		}
	}
	
	public void angleLabel(){
		if (cccomponent.getAngle()== 0.0){
			cccomponent.setLabel(0f,0f);
		}

	}
	
	public void storePage(int index, String pageFile){
		// store page in properties file
			try{
			String n = Integer.toString(index);
			properties.setProperty(pageFile, n);
			properties.store(new FileOutputStream(prevFile), null);
			System.out.println("STORED PAGE: " + n);

		} catch (IOException e){
			e.printStackTrace();
		}

		}
	public int retrievePage(String retrievedFile){
		try {
			properties.load(new FileInputStream(prevFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int page = 0;
		String pageNum = retrievedFile;		

		if(!properties.getProperty(pageNum).isEmpty()) // if property is empty then setproperty
		{	
			
			page = Integer.valueOf(properties.getProperty(pageNum));
		}

		return page;
	}
	
	// Match word in input file and return index
	//public int findWord(takes in word from text) {
	//read in input file
	//take in input from text box and put it to lower case
	//find if there is a match between word and file
	//return index of word
//}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		if (started){
		dmanager.updateResults(sentences.get(index), cccomponent.getAngle(), cccomponent.getIntensity(), cccomponent.getTouchpoint());
		}
		int result = JOptionPane.showConfirmDialog(this, "Do you want to save your work before closing?", "Circumplex Annotation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == JOptionPane.NO_OPTION){
			this.dispose();
		}else{
			actionPerformed(new ActionEvent(this, 0, "Save state"));
			JOptionPane.showMessageDialog(this, "Your work has been saved", "Circumplex Annotation", JOptionPane.INFORMATION_MESSAGE);
		}
		
		this.dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
