package censetagger.ui.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * The main dialog of ABCVTagger.
 * @author Tuomo Kakkonen
 *
 */
public class MainWindow extends JFrame {
	private MenuBar menu;
	private CENSeTagger tagger;
	
	public MainWindow(CENSeTagger profiler) {
		this.setTitle("ABCV Analyzer");
		this.tagger = profiler;
		menu = new MenuBar(this);
		setJMenuBar(menu);
		setSize(250, 250);
		setLocation(300,200);
		
		addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {
		    	  shutdown();
		      }
		    });
	}
	
	public void shutdown() {
		tagger.shutdownGate();
  	  	System.exit(0);
	}
	
	public CENSeTagger getProfiler() {
		return tagger;
	}
}
