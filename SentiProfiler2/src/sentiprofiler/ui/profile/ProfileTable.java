package sentiprofiler.ui.profile;

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

import sentiprofiler.profile.*;
import sentiprofiler.profile.SentimentVertex;
import sentiprofiler.profile.SentimentProfile;

/**
 * Table for viewing sentiment profiles.
 * @authors Tuomo Kakkonen
 *         Calkin Suero Montero
 *
 */
public class ProfileTable  extends JTable {
		private Vector<SentimentProfile> profs;
		private DefaultTableModel model;
		private String[] columnNames;
		private Object[][] cells;
		private int[] colWidth = {50, 100, 160, 120, 75, 75, 100, 75, 75};//{ 50, 120, 160, 120, 50, 50, 50};
		
		public ProfileTable() { // { "ID", "Name", "Creation time", "Ontology", "Classes", "Words", "Rel. words"}
			this(new String[]{ "ID", "Cat", "Creation time", "Ontology", "Classes", "Words", "Rel. words", "totalCat", "posToneg"});
		}
		
		public ProfileTable(String[] columnNames) {
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

		/**
		 * Returns the selected sentiment profiles.
		 * @return Selected sentiment profile objects.
		 */
		public Vector<SentimentProfile> getSelectedProfiles() {
			Vector<SentimentProfile> selProfs = new Vector<SentimentProfile>();
			if(getSelectedRowCount() == 1) {
				int selId = Integer.parseInt(model.getValueAt(getSelectedRow(), 0).toString());
				for (SentimentProfile p : profs)
					if (p.getId() == selId) {
						selProfs.add(p);
						return selProfs;
					}
			}
			if(getSelectedRowCount() == 2) {	
				int[] selRows = getSelectedRows();
				int selId1 = Integer.parseInt(model.getValueAt(selRows[0], 0).toString());
				int selId2 = Integer.parseInt(model.getValueAt(selRows[1], 0).toString());
				for (SentimentProfile p : profs)
					if (p.getId() == selId1 || p.getId() == selId2) 
						selProfs.add(p);
				return selProfs;
			}
			else {
				int[] selRows = getSelectedRows();
				//System.out.println("total profiles: " + selRows.length);
				for (SentimentProfile p : profs){
					for (int i=0; i < selRows.length ; i++ ){
						int selId= Integer.parseInt(model.getValueAt(selRows[i], 0).toString());
						if (p.getId()== selId)
							selProfs.add(p);
					}
				}	
			}
			
			return selProfs;
		}

		private void setColumnNames(String[] columnNames) {
			this.columnNames = columnNames;
		}

		/**
		 * Sets the table data.
		 * @param profs Vector of sentiment profiles.
		 */
		public void setData(Vector<SentimentProfile> profs) {
			this.profs = profs;
			if (profs != null) {
				List l = new LinkedList();
				for (SentimentProfile prof : profs) {
					Object[] cell = new Object[columnNames.length];
//					cell[0] = prof.getId();
//					cell[1] = prof.getName();
//					cell[2] = prof.getTime();
//					cell[3] = prof.getOntology().getId() + ", " + prof.getOntology().getName();
//					if(prof.getGraph() != null)
//						cell[4] = prof.getGraph().getVertexCount();
//					else cell[4] = "N/A";
//					cell[5] = prof.getSourceTokenCount();
//					cell[6] = prof.getSourceRelTokenCount();
					cell[0] = prof.getId();
					cell[1] = prof.getName();
					cell[2] = prof.getTime();
					cell[3] = prof.getOntology().getId() + ", " + prof.getOntology().getName();
					cell[4] = "N/A";
					cell[5] = prof.getSourceTokenCount();
					cell[6] = prof.getSourceRelTokenCount();
					cell[7] = prof.gettotalCat();
					double matchCount=0;
					matchCount = getPosNegRatio(prof.getVertex("negative-emotion"), prof.getVertex("positive-emotion"));
					matchCount = Math.round(matchCount*100)/100.00d;
					cell[8]=matchCount;
					l.add(cell);
				}
				cells = (Object[][]) l.toArray(new Object[0][]);
				model.setDataVector(cells, columnNames);
				setModel(model);
				setColumnWidths();
			}
		}

		private double getPosNegRatio(SentimentVertex neg, SentimentVertex pos) {
			if(pos == null)
				return 0;
			else if(pos.getAggregateValue(0) == 0) 
				return 0;
			else if(neg == null)
				return 1;
			else if(neg.getAggregateValue(0) == 0)
				return 1; 
			return (pos.getAggregateValue(0) / ( pos.getAggregateValue(0) + neg.getAggregateValue(0)));
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
			if ((row % 2) == 0) 
				return whiteRenderer;
			else
				return grayRenderer;
		}
}
