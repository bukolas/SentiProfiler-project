package abcvtagger.ui.vertexinfo;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JDialog;

import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.SentimentVertex;



/**
 * A GraphMousePlugin that brings up distinct popup menus when an edge or vertex is
 * appropriately clicked in a graph.  If these menus contain components that implement
 * either the EdgeMenuListener or VertexMenuListener then the corresponding interface
 * methods will be called prior to the display of the menus (so that they can display
 * context sensitive information for the edge or vertex).
 * @author Dr. Greg M. Bernstein
 */
public class VertexPopupMenuPlugin<V, E> extends AbstractPopupGraphMousePlugin {
    private PopupInfoDialog vertexPopup;
    
    /** Creates a new instance of PopupVertexEdgeMenuMousePlugin */
    public VertexPopupMenuPlugin() {
        this(MouseEvent.BUTTON3_MASK);
    }
    
    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     * @param modifiers mouse event modifiers see the jung visualization Event class.
     */
    public VertexPopupMenuPlugin(int modifiers) {
        super(modifiers);
    }
    
    /**
     * Implementation of the AbstractPopupGraphMousePlugin method. This is where the 
     * work gets done. You shouldn't have to modify unless you really want to...
     * @param e 
     */
    protected void handlePopup(MouseEvent e) {
        final VisualizationViewer<V,E> vv =
                (VisualizationViewer<V,E>)e.getSource();
        Point2D p = e.getPoint();
        
        GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
        if(pickSupport != null) {
            final V v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
            if(v != null) {
                updateVertexMenu(v, vv, p);
                vertexPopup.setLocation(e.getX(), e.getY());
                vertexPopup.show((SentimentVertex)v);
            }
        }
    }
    
    private void updateVertexMenu(V v, VisualizationViewer vv, Point2D point) {
        if (vertexPopup == null) return;
        Component[] menuComps = vertexPopup.getComponents();
        for (Component comp: menuComps) {
            if (comp instanceof VertexMenuListener) {
                ((VertexMenuListener)comp).setVertexAndView(v, vv);
            }
            if (comp instanceof MenuPointListener) {
                ((MenuPointListener)comp).setPoint(point);
            }
        }
                
    }
        
    /**
     * Getter for the vertex popup.
     * @return 
     */
    public JDialog getVertexPopup() {
        return vertexPopup;
    }
    
    /**
     * Setter for the vertex popup.
     * @param vertexPopup 
     */
    public void setVertexPopup(PopupInfoDialog vertexPopup) {
        this.vertexPopup = vertexPopup;
    }
    
    
    private interface VertexMenuListener<V> {
        void setVertexAndView(V v, VisualizationViewer visView);    
    }
    
    private interface MenuPointListener {
    	void   setPoint(Point2D point);        
    }


    
}
