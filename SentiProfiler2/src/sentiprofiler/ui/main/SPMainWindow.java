package sentiprofiler.ui.main;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * The main dialog of JittersMeter.
 * @author Tuomo Kakkonen
 *
 */
public class SPMainWindow extends JFrame {
	private SPMenuBar menu;
	private SentiProfiler profiler;
	
	public SPMainWindow(SentiProfiler profiler) {
		this.setTitle("SentiProfiler");
		this.profiler = profiler;
		menu = new SPMenuBar(this);
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
		profiler.shutdownGate();
  	  	System.exit(0);
	}
	
	public SentiProfiler getProfiler() {
		return profiler;
	}
}
