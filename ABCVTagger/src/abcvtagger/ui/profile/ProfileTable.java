package abcvtagger.ui.profile;

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
import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.SentimentVertex;
import abcvtagger.ui.profile.*;



/**
 * Table for viewing sentiment profiles.
 * @author Tuomo Kakkonen
 * @author Calkin Suero Montero
 *
 */
public class ProfileTable  extends JTable {
		private Vector<DocumentProfile> profs;
		private DefaultTableModel model;
		private String[] columnNames;
		private Object[][] cells;
		private int[] colWidth = {50, 50, 120, 160, 120, 75, 75, 100, 75, 75}; //{50, 50, 120, 160, 120, 50, 50, 50};
		
		public ProfileTable() {
			this(new String[] { "ID", "Cat", "Name", "Creation time", "Ontology", "Classes", "Words", "Rel. words", "totalCat", "posToneg"});
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
			//setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			setColumnWidths();
		}

		/**
		 * Returns the selected sentiment profiles.
		 * @return Selected sentiment profile objects.
		 */
		public Vector<DocumentProfile> getSelectedProfiles() {
			Vector<DocumentProfile> selProfs = new Vector<DocumentProfile>();
			if(getSelectedRowCount() == 1) {
				int selId = Integer.parseInt(model.getValueAt(getSelectedRow(), 0).toString());
				for (DocumentProfile p : profs)
					if (p.getId() == selId) {
						selProfs.add(p);
						return selProfs;
					}
			}
			if(getSelectedRowCount() == 2) {	
				int[] selRows = getSelectedRows();
				int selId1 = Integer.parseInt(model.getValueAt(selRows[0], 0).toString());
				int selId2 = Integer.parseInt(model.getValueAt(selRows[1], 0).toString());
				for (DocumentProfile p : profs)
					if (p.getId() == selId1 || p.getId() == selId2) 
						selProfs.add(p);
				return selProfs;
			}
			// this gets the profiles for categorization
			else {
				int[] selRows = getSelectedRows();
				for (DocumentProfile p : profs){
					for (int i : selRows){
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
		public void setData(Vector<DocumentProfile> profs) {
			this.profs = profs;
			if (profs != null) {
				//if (prof.getSourceRelTokenCount()>0){  //working only with profiles with hit
					List l = new LinkedList();
					for (DocumentProfile prof : profs) {
						if (prof.getSourceRelTokenCount()>0){  //working only with profiles with onto hit
							Object[] cell = new Object[columnNames.length];
							cell[0] = prof.getId();
							cell[1] = prof.getCategory();
							cell[2] = prof.getName();
							cell[3] = prof.getTime();
							cell[4] = prof.getOntology().getName();
							//if(prof.getGraph() != null)
							//	cell[4] = prof.getGraph().getVertexCount();
							//else 
							cell[5] = "N/A";
							cell[6] = prof.getSourceTokenCount();
							cell[7] = prof.getSourceRelTokenCount();
							cell[8] = prof.gettotalCat();
							double matchCount=0;
							matchCount = getPosNegRatio(prof.getSentimentVertex("negative-emotion"), prof.getSentimentVertex("positive-emotion"));
							matchCount = Math.round(matchCount*100)/100.00d;
							cell[9]=matchCount;
							l.add(cell);
						}
						//else{profs.remove(prof);}//deleteProf(prof);}
					}
						cells = (Object[][]) l.toArray(new Object[0][]);
						model.setDataVector(cells, columnNames);
						setModel(model);
						setColumnWidths();
				}
			//}
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
