package abcvtagger.ui.vertexinfo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*; 

import abcvtagger.profile.*;




/**
 * Popup dialog that opens when the user right-clicks a vertex in the graph.
 * @author Tuomo Kakkonen
 *
 */
public class PopupInfoDialog extends JDialog {
	private SentimentVertex curVertex;
	private String mainTitle;
	private WordCloudCreator tagCloud = new WordCloudCreator();
	private DefaultListModel model = new DefaultListModel();
	private JList list = new JList(model);
	private JScrollPane listScroller = new JScrollPane(list);
	private JPanel cloudPanel, mainPanel, buttonPanel;
	private Vector<WordContext> contexts;
	private JButton showAllButton;
	private CloudViewerPanel htmlPanel;
	private DocumentProfile profile;
	
	public PopupInfoDialog(DocumentProfile profile) {
		this.profile = profile;
		setLayout(new BorderLayout());
		cloudPanel = new JPanel(new BorderLayout());
		mainPanel = new JPanel(new BorderLayout());
		
		JPanel listPanel = new JPanel(new BorderLayout());
		JButton remButton = new JButton("Remove");
		remButton.addActionListener(
			    new ActionListener() {
			        public void actionPerformed(ActionEvent e) {
			        	System.out.println("Remove context, update values, if changed update graph after closing.");
			        	removeContext(list.getSelectedIndex());
			        }
			    }
			);		
		listPanel.add(listScroller, BorderLayout.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(remButton, BorderLayout.EAST);
		listPanel.add(panel, BorderLayout.EAST);

		mainPanel.add(listPanel, BorderLayout.NORTH);
		mainPanel.add(cloudPanel, BorderLayout.SOUTH);
		mainPanel.add(new JPanel(), BorderLayout.CENTER);

		buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(
		    new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	setVisible(false);
		        }
		    }
		);
		showAllButton = new JButton("Show all");
		showAllButton.addActionListener(
		    new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	showAll();
		        }
		    }
		);

		buttonPanel.add(okButton);
		
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.WEST);
		add(new JPanel(), BorderLayout.EAST);
		add(buttonPanel, BorderLayout.SOUTH);
		add(mainPanel, BorderLayout.CENTER);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}
	
	public void removeContext(int ind) {
    	model.removeElementAt(ind);
    	profile.removeContext(curVertex, ind);
	}
	
	public void show(SentimentVertex v) {
		mainTitle = v.toString2() + " (" + v.getContexts().size() + " contexts) "; 
		setTitle(mainTitle);		
		curVertex = v;
		contexts = v.getContexts();

		tagCloud.makeContextTagCloud(contexts);
		htmlPanel = new CloudViewerPanel(this, tagCloud);
		cloudPanel.removeAll();
		cloudPanel.add(htmlPanel, BorderLayout.CENTER) ;
		System.out.println("Comps: " + cloudPanel.getComponentCount());
		JPanel bPanel = new JPanel(new BorderLayout());
		bPanel.add(showAllButton, BorderLayout.CENTER);
		cloudPanel.add(bPanel, BorderLayout.EAST);

		setVisible(true);
		updateTable(contexts);
		pack();
	}
	
	private void updateTable(Vector<WordContext> cons) {
		model.removeAllElements();
		for(WordContext context : cons) 
			model.addElement(context.getContext());
		}

	public void showAll() {
		setTitle(mainTitle);		
		updateTable(contexts);		
	}
	
	public void filter(String word) {
		Vector<WordContext> filteredContexts = new Vector<WordContext>();
		for(WordContext c : contexts) {
			if(word.equalsIgnoreCase(c.getWord()))
				filteredContexts.add(c);
		}
		setTitle(mainTitle + "- " + word + "(" + filteredContexts.size() + " contexts)");		
		updateTable(filteredContexts);
	}
}
