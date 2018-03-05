package sentiprofiler.ui.ontology;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import sentiprofiler.profile.WNAffectOntology;

/**
 * Panel for viewing ontologies.
 * @author Tuomo Kakkonen
 *
 */
public class OntologySelectionPanel extends JPanel {
	protected JScrollPane panel;
	protected OntologyTable table;
	
	public OntologySelectionPanel() {
		this.setLayout(new BorderLayout());
		table = new OntologyTable();
		panel = new JScrollPane(table);
		add(new JLabel("Ontology"), BorderLayout.WEST);
		add(panel, BorderLayout.CENTER);				
	}
	
	public void update(Vector<WNAffectOntology> ontologies) {
		table.setData(ontologies);
	}
	
	public WNAffectOntology getSelection() {
		return table.getSelectedOntology();
	}
}
