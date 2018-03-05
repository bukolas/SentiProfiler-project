package sentiprofiler.ui.ontology;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import sentiprofiler.profile.WNAffectOntology;
import sentiprofiler.profile.SentimentProfile;

/**
 * Table for viewing ontologies.
 * @author Tuomo Kakkonen
 *
 */
public class OntologyTable  extends JTable {
		private Vector<WNAffectOntology> ontos;
		private DefaultTableModel model;
		private String[] columnNames;
		private Object[][] cells;
		private int[] colWidth = { 30, 70, 500};
		
		public OntologyTable() {
			this(new String[] { "ID", "Name", "Hierarchy file"});
		}
		
		public OntologyTable(String[] columnNames) {
			super();
			this.columnNames = columnNames;
			cells = new Object[1][columnNames.length];
			model = new DefaultTableModel(cells, columnNames) {
				public boolean isCellEditable(int row, int col) {
					return false; // this will make all the table cells uneditable
				}
			};
			setModel(model);
			setColumnNames(columnNames);
			setColumnWidths();

		}

		public OntologyTable(TableModel tm) {
			super(tm);
		}

		public OntologyTable(Object[][] data, Object[] columns) {
			super(data, columns);
		}

		public OntologyTable(int rows, int columns) {
			super(rows, columns);
		}

		/**
		 * Returns the selected ontology.
		 * @return Selected ontology.
		 */
		public WNAffectOntology getSelectedOntology() {
			if(getSelectedRow() == -1) 
				return null;
			int selId = Integer.parseInt(model.getValueAt(getSelectedRow(), 0).toString());
			for (WNAffectOntology o : ontos)
				if (o.getId() == selId) 
					return o;
			return null;
		}

		private void setColumnNames(String[] columnNames) {
			this.columnNames = columnNames;
		}

		/**
		 * Sets the table data.
		 * @param profs Vector of ontologies.
		 */
		public void setData(Vector<WNAffectOntology> ontos) {
			this.ontos = ontos;
			if (ontos != null) {
				List l = new LinkedList();
				for (WNAffectOntology onto : ontos) {
					Object[] cell = new Object[columnNames.length];
					cell[0] = onto.getId();
					cell[1] = onto.getName();
					cell[2] = onto.getHierarchyFilename();
					l.add(cell);
				}
				cells = (Object[][]) l.toArray(new Object[0][]);
				model.setDataVector(cells, columnNames);
				setModel(model);
				setColumnWidths();
			}
		}

		/**
		 * Sets widths of the columns.
		 * @param c Index of the column.
		 * @param width Width of the column.
		 * @param columnModel Column model of the table.
		 */
		private void setColumnWidth(int c, int width, TableColumnModel columnModel) {
			TableColumn column = columnModel.getColumn(c);
			column.setMinWidth(width);
			column.setMaxWidth(width);
			column.setPreferredWidth(width);
			column.setWidth(width);
			column.setResizable(false);
		}

		/**
		 * Sets the column widths.
		 */
		private void setColumnWidths() {
			TableColumnModel columnModel = getColumnModel();			
			for (int x = 0; x < columnModel.getColumnCount(); x++) {
				setColumnWidth(x, colWidth[x], columnModel);
			}
		}

		private DefaultTableCellRenderer whiteRenderer = new DefaultTableCellRenderer();
		private DefaultTableCellRenderer grayRenderer = new DefaultTableCellRenderer();
		{
			grayRenderer.setBackground(Color.LIGHT_GRAY);
		}

		/**
		 * If row is an even number, getCellRenderer() returns a DefaultTableCellRenderer with white background. For odd
		 * rows, this method returns a DefaultTableCellRenderer with a light gray background.
		 */
		public TableCellRenderer getCellRenderer(int row, int column) {
			if ((row % 2) == 0) {
				return whiteRenderer;
			} else {
				return grayRenderer;
			}
		}
}
