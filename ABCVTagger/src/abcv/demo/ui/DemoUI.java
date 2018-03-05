package abcv.demo.ui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.JFrame;

import abcvtagger.ui.main.ABCVTagger;
import abcvtagger.ui.main.MainWindow;

public class DemoUI extends JFrame {
	 private ABCVTagger tagger;
	 private Panels pan;
	 public DemoUI(ABCVTagger profiler) {
	        //Create and set up the window.
	        JFrame frame = new JFrame("DemoUI");
	       // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        this.tagger = profiler;
	        
	        frame.add(new Panels(this)); //need to pass the tagger to panels.java
	       // JLabel emptyLabel = new JLabel("");
	        //emptyLabel.setPreferredSize(new Dimension(175, 100));
	        //frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
	       
	        try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        SwingUtilities.updateComponentTreeUI(frame);
	 
	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	        
	        frame.addWindowListener(new WindowAdapter() {
			      public void windowClosing(WindowEvent e) {
			    	  shutdown();
			      }
			    });
	    }
	 
	 
	 public void shutdown() {
			tagger.shutdownGate();
	  	  	System.exit(0);
		}
		
		public ABCVTagger getProfiler() {
			return tagger;
		}
	 
//	 public static void main(String[] args) {
//	        //Schedule a job for the event-dispatching thread:
//	        //creating and showing this application's GUI.
//	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//	            public void run() {
//	                createAndShowGUI();
//	            }
//	        });
//	        
//	       
//	    }

}
