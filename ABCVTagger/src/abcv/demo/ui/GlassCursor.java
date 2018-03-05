package abcv.demo.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GlassCursor implements Cursors {
	private final static MouseAdapter mouseAdapter = new MouseAdapter() {};
	private GlassCursor() {}
	
	/** Sets cursor for specified component to Wait cursor */
	  public static void startWaitCursor(JComponent component) { 
	    RootPaneContainer root =
	      ((RootPaneContainer) component.getTopLevelAncestor()); 
	    root.getGlassPane().setCursor(WAIT_CURSOR);
	    root.getGlassPane().addMouseListener(mouseAdapter);
	    root.getGlassPane().setVisible(true);
	  }

	  /** Sets cursor for specified component to normal cursor */
	  public static void stopWaitCursor(JComponent component) { 
	    RootPaneContainer root =
	      ((RootPaneContainer) component.getTopLevelAncestor()); 
	    root.getGlassPane().setCursor(DEFAULT_CURSOR);
	    root.getGlassPane().removeMouseListener(mouseAdapter);
	    root.getGlassPane().setVisible(false);
	  }
}
