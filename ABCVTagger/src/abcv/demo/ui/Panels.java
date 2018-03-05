package abcv.demo.ui;

import gate.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
 
import java.awt.*;              //for layout managers and more
import java.awt.event.*;        //for action events
import java.awt.image.BufferedImage;
 
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import abcvtagger.AbstractABCVTagger;
import abcvtagger.ProfileAndOntologyManager;
import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.SentimentVertex;
import abcvtagger.ui.*;
import abcvtagger.ui.main.*;
import abcvtagger.ui.profile.ProfileCreatorDialog;
import abvc.classifier.Classifier;


public class Panels extends JPanel implements ActionListener {
	
	private JTextArea textArea;
	//ABCVTagger tag;
	private DemoUI parent;
	
	private String classResult="";
	
	JPanel rightPane = new JPanel(new BorderLayout());
	
	 public Panels(DemoUI parent) {
		 this.parent = parent;
		// setLayout(new BorderLayout());
	//Create a text area.
    textArea = new JTextArea(
            "This is an editable JTextArea. " +
            "A text area is a \"plain\" text component, " +
            "which means that although it can display text " +
            "in any font, all of the text is in the same font."
    );
    textArea.setFont(new Font("Serif", Font.ITALIC, 16));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    JScrollPane areaScrollPane = new JScrollPane(textArea);
    areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    areaScrollPane.setPreferredSize(new Dimension(500, 500));
    areaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Plain Text"),
                            BorderFactory.createEmptyBorder(5,5,5,5)),
            areaScrollPane.getBorder()));
 
    // classify button
    JButton classifyButton = new JButton("Classify");
    classifyButton.addActionListener(this);
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(classifyButton);
 //   buttonPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
  //Put left panel together.
    JPanel leftPane = new JPanel(new BorderLayout());
    leftPane.add(areaScrollPane, BorderLayout.CENTER);
    leftPane.add(buttonPane,BorderLayout.SOUTH);
    //JPanel buttonPane = new JPanel();
    
    //Right panel to display results
//    JPanel rightPane = new JPanel(new BorderLayout());
//    rightPane.setBorder(new TitledBorder("Results"))	;
    rightPane.setPreferredSize(new Dimension(500,525));
    rightPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Results"),
            BorderFactory.createEmptyBorder(5,5,5,5)),rightPane.getBorder()));
//    try {
//		BufferedImage myPicture = ImageIO.read(new File("female.png"));
//		JLabel picLabel = new JLabel("Predicted Class",new ImageIcon(myPicture),JLabel.CENTER);
//		rightPane.add(picLabel);
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//    
    
  
    // put everything together and visualize the panel
    add(leftPane, BorderLayout.LINE_START);
    add (rightPane,BorderLayout.LINE_START);
    //add(classifyButton,BorderLayout.PAGE_END); //this puts the button on the side

    
	 }

   
	 
	 
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
//		rightPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		abcv.demo.ui.GlassCursor.startWaitCursor(rightPane);
		String toClassify = textArea.getText();
		createProfile(toClassify);
		//System.out.println("***** text sent : " + toClassify);
		//tag = new ABCVTagger(toClassify, getAutoscrolls());
		
		}
	private void createProfile(String s) {
		//boolean ok = parent.getProfiler().analyzeText(s); // need to get the doc profile for actionTaken
		//if (ok == true){
		//	System.out.println("***** profile created ok? : " + ok);		
		//}
		
		DocumentProfile p =parent.getProfiler().analyzeTextDemo(s) ;
		actionTaken(p);
	}
	protected void actionTaken (DocumentProfile p){
	
		ProfileAndOntologyManager profMan = parent.getProfiler().getProfileManager();
	
		Classifier cl = new Classifier(profMan, p,true,true,true,true,true,true);
		
		Vector<SentimentVertex> classes = p.getClasses();
		System.out.println("***** classes : " + classes);
		String [] vetArr= new String[classes.size()];
		int foo=0;
		for (SentimentVertex c : classes) {
			vetArr[foo]=c.getNameWithoutFrequency();
			foo++;
		}
		java.util.List<String> list = Arrays.asList(vetArr);
		classResult = cl.FinalPrediction();
		double classProb=0;
		try {
			classProb=cl.ClassProbability();
			if (classProb>1) classProb=1.00;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String tmp = String.format("%.3g%n",classProb);
		try {
			if (classResult=="Female"){
				BufferedImage myPicture = ImageIO.read(new File("female.png"));
				JLabel picLabel = new JLabel("<html>Predicted Gender: FEMALE<br>Class Probability: "+ 
								tmp + "<br>Used emotions: "+list+"</html>",new ImageIcon(myPicture),JLabel.CENTER);
				picLabel.setVerticalTextPosition(JLabel.BOTTOM);
				picLabel.setHorizontalTextPosition(JLabel.CENTER);
				picLabel.setIconTextGap(10);
				picLabel.setFont(new Font("Serif", Font.BOLD, 14));
			//	rightPane.setCursor(Cursor.getDefaultCursor());
				abcv.demo.ui.GlassCursor.stopWaitCursor(rightPane);
				rightPane.removeAll();
				rightPane.add(picLabel);
				revalidate(); // to repaint the panel
				repaint();
			}
			else {
				BufferedImage myPicture = ImageIO.read(new File("male.png"));
				JLabel picLabel = new JLabel("<html>Predicted Gender: MALE<br>Class Probability: "+
						tmp + "<br>Used emotions: "+list+"</html>"+ "</html>",new ImageIcon(myPicture),JLabel.CENTER);
				picLabel.setVerticalTextPosition(JLabel.BOTTOM);
				picLabel.setHorizontalTextPosition(JLabel.CENTER);
				picLabel.setIconTextGap(10);
				picLabel.setFont(new Font("Serif", Font.BOLD, 14));
				//rightPane.setCursor(Cursor.getDefaultCursor());
				abcv.demo.ui.GlassCursor.stopWaitCursor(rightPane);
				rightPane.removeAll();
				rightPane.add(picLabel);
				revalidate(); // to repaint the panel
				repaint();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//remove profile to prepare for another one
		profMan.deleteProfile(p);
	} 
	
	
}
