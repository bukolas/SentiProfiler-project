package sentiprofiler.ui.profile;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import org.apache.commons.collections15.Transformer;
import sentiprofiler.Constants;
import sentiprofiler.profile.SentimentProfile;
import sentiprofiler.profile.SentimentVertex;
import sentiprofiler.ui.vertexinfo.PopupInfoDialog;
import sentiprofiler.ui.vertexinfo.VertexPopupMenuPlugin;
import sentiprofiler.utils.Utils;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Visualizes sentiment profiles.
 * 
 * @authors Tuomo Kakkonen
 * 		   Calkin Suero Montero
 * 
 */
public class SentimentProfileVisualizer extends JDialog {
	private SentimentProfile profile;
	private String name;
	private int measureInd = -1;
	private boolean showFreq;
	private VisualizationViewer<SentimentVertex, String> viewer;
	private DirectedSparseMultigraph<SentimentVertex, String> curGraph, origGraph;
	private Layout<SentimentVertex, String> layout;
	private JCheckBox smallerButton, largerButton, additionalButton, freqButton;
	private JSlider slider;
	private JRadioButton measureButton1, measureButton2;
	
	public SentimentProfileVisualizer(SentimentProfile profile) {
		this.profile = profile;
	}
	
	/**
	 * Switches to the comparison mode updates the graph that has the comparison
	 * results.
	 */
	public void setComparisonMode(DirectedSparseMultigraph<SentimentVertex, String> graph) {
		//System.out.println("slider value "+ slider.getValue());
		origGraph = makeCopy(graph);
		updateGraph(slider.getValue());
		smallerButton.setEnabled(true);
		largerButton.setEnabled(true);
		additionalButton.setEnabled(true);
	}

	/**
	 * Sets the index of the measure to show in the
	 * node visualizations.
	 * @param measureInd Index of the measure to use (-1= no measure). 
	 */
	public void setMeasureInd(int measureInd) {
		this.measureInd = measureInd;
	}
	
	/**
	 * Gets the current measure index.
	 * @return Measure index.
	 */
	public int getMeasureInd() {
		return measureInd;
	}
	
	private static SentimentVertex getVertex(DirectedSparseMultigraph<SentimentVertex, String> graph, 
			String vName) {
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
		for(SentimentVertex v : vertices)
			if(v.getName() == vName) return v;
		return null;		
	}
	
	/**
	 * Returns a copy of the graph given as the parameter.
	 * @param oGraph
	 * @return Deep copy of the graph.
	 */
	public static DirectedSparseMultigraph<SentimentVertex, String> makeCopy(
			DirectedSparseMultigraph<SentimentVertex, String> oGraph) {
		DirectedSparseMultigraph<SentimentVertex, String> newGraph =
			new DirectedSparseMultigraph<SentimentVertex, String>();
		Vector<String> edges = new Vector<String>(oGraph.getEdges());
		for(String e : edges)	{		
			Pair<SentimentVertex> vs = oGraph.getEndpoints(e);
			SentimentVertex v1 = getVertex(newGraph, vs.getFirst().getName());
			if(v1 == null) 
				v1 = vs.getFirst().copy();
			SentimentVertex v2 = getVertex(newGraph, vs.getSecond().getName());
			if(v2 == null) 
				v2 = vs.getSecond().copy();			
			newGraph.addEdge(e, v1, v2);
		}
		return newGraph;
	}
	
	/**
	 * Visualizes the graph given as a parameter.
	 * 
	 * @param name
	 *            Name of the graph.
	 * @param graph
	 *            Graph object.
	 */
	public void visualize(String name,
			DirectedSparseMultigraph<SentimentVertex, String> graph) {
		this.name = name;
		origGraph = makeCopy(graph);
		curGraph = makeCopy(graph);
		
		DelegateForest<SentimentVertex, String> forest = new DelegateForest<SentimentVertex, String>(curGraph);
		layout = new TreeLayout<SentimentVertex, String>(forest, 150, 100);

		viewer = new VisualizationViewer<SentimentVertex, String>(layout);
		viewer.setPreferredSize(new Dimension(800, 600));
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.S); // it can be N E W NE NW SE SW CNTR AUTO

		setTransformers();
		setMouseAndPopup();
		makeDialog();
	}

	private void setMouseAndPopup() {
		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		viewer.setGraphMouse(gm);

		VertexPopupMenuPlugin plugin = new VertexPopupMenuPlugin();
		plugin.setVertexPopup(new PopupInfoDialog(profile));
		gm.add(plugin);
	}

	private void setTransformers() {
		float dash[] = { 10.0f };
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
			public Stroke transform(String s) {
				return edgeStroke;
			}
		};

		Transformer<SentimentVertex, Paint> vertexFillTrans = new Transformer<SentimentVertex, Paint>() {
			public Paint transform(SentimentVertex v) {
				if (v.getComparisonFlag() == 0)
					return Color.RED;
//					return Color.WHITE;  //smaller vertices
				if (v.getComparisonFlag() == 1)
					return Color.BLUE;   // additional vertices
//					return Color.LIGHT_GRAY;
				else
					return Color.GREEN; //comparison flag==2
//					return Color.BLACK; //bigger vertices
			}
		};

		Transformer<SentimentVertex, Font> vertexFontTrans = new Transformer<SentimentVertex, Font>() {
			public Font transform(SentimentVertex v) {
				return new Font(Font.SANS_SERIF, Font.PLAIN, 16);
			}
		};

		Transformer<SentimentVertex, String> vertexLabeler = new Transformer<SentimentVertex, String>() {
			public String transform(SentimentVertex v) {
				String str = v.getName();
				if(isShowFrequencies()) 
					str += " " + v.getFrequency();
					//str += " " + v.getComparisonFlag();
				int mInd = getMeasureInd();
				//if (mInd ==0){
				if(mInd > -1) {
					str += " " + Utils.getTwoDecimals(v.getValue(mInd));
					if(v.getAggregateValue(mInd) > 0)
						str += " (" + Utils.getTwoDecimals(v.getAggregateValue(mInd)) + ")";
				}
				return str;
			}
		};

		
		viewer.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		//viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		viewer.getRenderContext().setVertexLabelTransformer(vertexLabeler);

		viewer.getRenderContext().setVertexFillPaintTransformer(vertexFillTrans);
		viewer.getRenderContext().setVertexFontTransformer(vertexFontTrans);
		// vv.getRenderContext().setEdgeLabelTransformer(new
	}
	
	/**
	 * Gets the maximum JMA score on the second level from the root.
	 * @return maximum value of the slide (taken from freq values or aggregated values)
	 */
	private int getMaxSliderValue() {
		double max = 0;
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(curGraph.getVertices());
		
		// Exclude the root and its successors.
		Vector<SentimentVertex> topVertices = new Vector<SentimentVertex>();
		for (SentimentVertex v : vertices) {
			if(curGraph.getPredecessors(v).size() == 0) {
				topVertices.add(v);
				topVertices.addAll(curGraph.getSuccessors(v));
			}
		}
		System.out.println("measureInd " + measureInd);
		for (SentimentVertex v : vertices){
			//when the measureInd is given
			if(measureInd > -1){
				if(v.getAggregateValue(measureInd) > max && !topVertices.contains(v)) {
					max = v.getAggregateValue(measureInd);}
			}
			//using measureInd=0 as default/maybe using the freq instead? 
			else {
				//use measureInd 0 = tokens / total word tokens
				//if(v.getFrequency() > max) 	max = v.getFrequency();
				max= curGraph.getVertexCount();
			}
		}
		
		
		System.out.println("slider max value " + max);
		
		return (int)(max + 0.5);			
	}

	private boolean checkSubGraph(DirectedSparseMultigraph<SentimentVertex, String> graph,
			SentimentVertex v, int value) {
		if(v.getComparisonFlag() == value) return true;
		else {
			Vector<SentimentVertex> vs = new Vector<SentimentVertex>(graph.getSuccessors(v));
			for(SentimentVertex v2 : vs)
				if(checkSubGraph(graph, v2, value)) return true;
		}
		return false;
	}

	
	private void filterVertices(DirectedSparseMultigraph<SentimentVertex, String> graph,
			boolean add, boolean larger, boolean small) {
		if(add && larger && small) return;
		if(!add && !larger && !small) {
			Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
			for (SentimentVertex v : vertices) 
				graph.removeVertex(v);
			return;
		}
		
		System.out.println("Color filtering starts with " + graph.getVertexCount());
		
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
		for (SentimentVertex v : vertices) {
			boolean keep = true;
			if(add) 
				keep = checkSubGraph(graph, v, Constants.ADDITIONAL_VERTEX);
			if(!keep && larger) 
				keep = checkSubGraph(graph, v, Constants.BIGGER_VERTEX);
			if(!keep && small) 
				keep = checkSubGraph(graph, v, 0);
			if(!keep) 
				graph.removeVertex(v);
		}
		System.out.println("Color filtering done: " + graph.getVertexCount());

	}
		
	/**
	 * Update the graph.
	 * @param limit Minimum AJM value.
	 */
	private void updateGraph(int limit) {	
		curGraph = makeCopy(origGraph);
		int tmp = getMaxSliderValue();
		System.out.println("Pruning. The graph has " + curGraph.getVertexCount() + " nodes.");
		System.out.println("Limit: " + limit);
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(curGraph.getVertices());
		if(measureInd > -1){
			for (SentimentVertex v : vertices){
					if(v.getAggregateValue(measureInd) < limit && v.getAggregateValue(measureInd) != 0)
					curGraph.removeVertex(v);
			}
		System.out.println("Removing from AggVal Done. " + curGraph.getVertexCount() + " left.");
		}
		if (measureInd == -1){
			System.out.println("limit2: "+ limit +" maxslidevalue: " + tmp);
			for (SentimentVertex v : vertices){
				if((v.getFrequency()>(tmp/(limit+1))|v.getFrequency()<=0) && curGraph.getSuccessorCount(v)<1)curGraph.removeVertex(v);
						
			}
			System.out.println("Removing from Freq Done. " + curGraph.getVertexCount() + " left.");
		}
		// Remove lonely leaves that are not connected to the rest of the graph.
		vertices = new Vector<SentimentVertex>(curGraph.getVertices());
		for (SentimentVertex v : vertices) {
			if(curGraph.getPredecessorCount(v) == 0 && curGraph.getSuccessorCount(v) == 0) 
				curGraph.removeVertex(v);
		}
		System.out.println("Removing lonely leaves Done. " + curGraph.getVertexCount() + " left.");		
		
		// Remove subgraphs that contain colors (types of vertices) that have not been 
		// selected to be viewed.
		filterVertices(curGraph,
				additionalButton.isSelected(), largerButton.isSelected(), smallerButton.isSelected() );
		
		DelegateForest<SentimentVertex, String> forest = new DelegateForest<SentimentVertex, String>(curGraph);
		layout.setGraph(forest);
		viewer.repaint();
		
	}
	private void updatePanel(){
		//getMaxSliderValue();
		makeControlsPanel().validate();
		makeControlsPanel().repaint();
	}
	
	public void setShowFrequencies(boolean show) {
		this.showFreq = show;
	}

	public boolean isShowFrequencies() {
		return showFreq;
	}

	
	/**
	 * Creates the panel with controls and buttons.
	 * @return
	 */
	private JPanel makeControlsPanel() {
		JPanel panel = new JPanel();
		
		//creating the slider
		int maxVal = getMaxSliderValue();
		slider = new JSlider(JSlider.HORIZONTAL, 0, maxVal, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					//System.out.println("source.getvalue. " + source.getValue());
					updateGraph(source.getValue());
				}
			}
		});
		slider.setMajorTickSpacing(maxVal / 4);
		slider.setMinorTickSpacing(maxVal / 8);		
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		//creating comparison buttons
		smallerButton = new JCheckBox("Smaller");
		smallerButton.setSelected(true);
		smallerButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateGraph(slider.getValue());
			}
		});

		largerButton = new JCheckBox("Larger");
		largerButton.setSelected(true);
		largerButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateGraph(slider.getValue());
			}
		});

		additionalButton = new JCheckBox("Additonal");
		additionalButton.setSelected(true);
		additionalButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateGraph(slider.getValue());
			}
		});
		
		//adding slider and buttons to the sub-panels
		TitledBorder border = BorderFactory.createTitledBorder("Comparison mode");
		border.setTitleJustification(TitledBorder.LEFT);
		JPanel checkPanel = new JPanel();
		checkPanel.setBorder(border);
		checkPanel.add(smallerButton);
		checkPanel.add(largerButton);
		checkPanel.add(additionalButton);

		TitledBorder border2 = BorderFactory.createTitledBorder("Filtering nodes");
		border2.setTitleJustification(TitledBorder.LEFT);
		JPanel sliderPanel = new JPanel();
		sliderPanel.setBorder(border2);
		sliderPanel.add(slider);

		//creating freq and measures buttons
		measureButton1 = new JRadioButton("emoVertex/Doc");
		measureButton1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(!measureButton1.isSelected() && !measureButton2.isSelected())
					setMeasureInd(-1);
				else {
					if(measureButton1.isSelected()) {
						measureButton2.setSelected(false);
						setMeasureInd(0);
						getMaxSliderValue();
						slider.validate();
						slider.repaint();
					}
				}
				//updatePanel();
				updateGraph(slider.getValue());
				
			}
		});

		measureButton2 = new JRadioButton ("emoVer/allEmotions");		
		measureButton2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(!measureButton2.isSelected() && !measureButton1.isSelected())
					setMeasureInd(-1);
				else {
					if(measureButton2.isSelected()) {
						measureButton1.setSelected(false);
						setMeasureInd(1);
						getMaxSliderValue();
						slider.validate();
						slider.repaint();
					}
				}
				//updatePanel();
				updateGraph(slider.getValue());
				
			}
		});

		freqButton = new JCheckBox("Frequency");
		setShowFrequencies(true);
		freqButton.setSelected(true);
		freqButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setShowFrequencies(freqButton.isSelected());
				getMaxSliderValue();
				//updatePanel();
				slider.validate();
				slider.repaint();
				updateGraph(slider.getValue());
			}
		});

		
		
		//adding freq and measures sub-panel
		TitledBorder border3 = BorderFactory.createTitledBorder("Node info");
		border3.setTitleJustification(TitledBorder.LEFT);
		JPanel vertexSetupPanel = new JPanel();
		vertexSetupPanel.setBorder(border3);
		vertexSetupPanel.add(freqButton);
		vertexSetupPanel.add(measureButton1);
		vertexSetupPanel.add(measureButton2);
				
		//creating the complete panel
		panel.add(vertexSetupPanel);
		panel.add(checkPanel);
		panel.add(new JPanel());
		panel.add(sliderPanel);
		
		//this buttons will be enable in comparison mode
		smallerButton.setEnabled(false);
		largerButton.setEnabled(false);
		additionalButton.setEnabled(false);
		
		return panel;
	}

	/**
	 * Creates and shows the frame that holds the graph view 
	 * and the controls. 
	 */
	private void makeDialog() {
		setTitle("SentiProfiler - " + name);
		JPanel controlPanel = makeControlsPanel();
		JPanel bottomPanel = new JPanel(new BorderLayout());
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {  
		        	close();
		       }
			}
		);  

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		bottomPanel.add(controlPanel, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(viewer, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);

		getContentPane().add(mainPanel);
		pack();
		setVisible(true);
	}
		
	private void close() {
		setVisible(false);
	}
}
