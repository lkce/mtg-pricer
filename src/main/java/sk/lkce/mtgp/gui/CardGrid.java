package sk.lkce.mtgp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import sk.lkce.mtgp.domain.Card;
import sk.lkce.mtgp.tablemodel.Cell;
import sk.lkce.mtgp.tablemodel.MtgPricerTableModel;
import sk.lkce.mtgp.tablemodel.MtgPricerTableModel.MtgPricerColumn;


/**
 * A table view component which shows card list and the pricing results for 
 * each card finder and card.
 */
@SuppressWarnings("serial")
public class CardGrid extends JPanel {

	private final static int NAME_COLUMN_MIN_WIDTH = 90;
	private final static int QUANTITY_COLUMN_MIN_WIDTH = 40;
	private final static int RESULT_COLUMN_MIN_WIDTH = 90;
	
	private JTable table;
	private JScrollPane scrollPane;
	private Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
	private MtgPricerTableModel tableModel;
	private Set<CardGridListener> listeners = new HashSet<>();
	private InternalTableListener internalTableListener;
	private int lastColumnCount;
	private int firstSelectedRow;
	private int lastSelectedRow;
	
	/**
	 * Creates a card grid backed by the specified table model.
	 * @param tableModel application table model
	 */
	public CardGrid(MtgPricerTableModel tableModel){   
		this.tableModel = tableModel;
		internalTableListener = new InternalTableListener();
		tableModel.addTableModelListener(internalTableListener);
		setupTable();
		setUpGui();
	}
	
	/**
	 * Adds as card grid listener to receive specific events
	 * from this card grid.
	 * @param listener
	 */
	public void addGridListener(CardGridListener listener){
		listeners.add(listener);
		
	}

	/**
	 * Returns the list of cards which are currently selected.
	 * @return
	 */
	public Collection<Card> getSelectedCards(){
		int[] rows= table.getSelectedRows();

		List<Card> l = new ArrayList<Card>();
		for (int i : rows)
			l.add(tableModel.getCardAt(i));
		return l;
	}
	
	/**
	 * Registers an action with this component and binds it to the {@link KeyStroke} returned by
	 * {@link Action#getValue(String)} with {@link Action#ACCELERATOR_KEY} as an argument.  In other words,
	 * when this component has focus, the registered action can be triggered by the action's keyboard shortcut.
	 * 
	 * If the returned
	 * value is null, the exception is thrown.
	 * 
	 * @param action action to be bind to this card grid
	 * @throws IllegalArgumentException if action has no accelerator key value
	 */
	public void registerAction(Action action){
		KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
		if (keyStroke == null)
			throw new IllegalArgumentException("The action must have non-null ACCELERATOR_KEY value");
		
		table.getInputMap().put(keyStroke, action.getValue(Action.NAME));
		table.getActionMap().put(action.getValue(Action.NAME),action);
	}

	/**
	 * Sets whether the rows in this card grid can be selected.
	 * @param b <code>true</code> if the row selection should be allowed, 
	 * 	<code>false</code> if it should be prohibited
	 */
	public void setRowSelectionAllowed(boolean b){
		table.setRowSelectionAllowed(b);
	}
	
	
	/**
	 * Calculates and returns the maximum column width among all the cells in a column if it is higher
	 * than passed minimum width value. If not, the minimum width is returned.
	 * 
	 * @param column the index of the column
	 * @param minWidth the initial minimum width
	 * @return maximum column width of the column or initial minimal width if it is higher
	 */
	private int getMaxColumnWidth(int column, int minWidth){
		int width = minWidth;
		
		TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        Component comp = headerRenderer.getTableCellRendererComponent(
        		table, table.getColumnName(column), false, false, 0, 0);
        width = Math.max(width,comp.getPreferredSize().width);
        //Loop all rows and look for widest cell so far.

		for (int row = 0; row < table.getRowCount(); row++) {
			TableCellRenderer  renderer = table.getCellRenderer(row, column);
			
			table.prepareRenderer(renderer, row, column);
			comp = table.prepareRenderer(renderer, row, column);
			
			width = Math.max (comp.getPreferredSize().width, width);
		    
		}
		return width;
	}
	
	/**
	 * Automatically calculates and sets the maximum required width
	 * for all the columns.
	 */
	private void setColumnsAutoWidth() {
		assert SwingUtilities.isEventDispatchThread();
		
		final int margin = 2;
		final TableColumnModel model = table.getColumnModel();

		final int[] widths = new int[model.getColumnCount()];
		
		for (int i = 0; i < widths.length; i++){
			int w = getMaxColumnWidth(i, model.getColumn(i).getMinWidth());
			widths[i] = w + margin;
		}

		for ( int i = 0; i < widths.length; i++){ //In case the number of columns has changed
			TableColumn col = model.getColumn(i);
			if (col.getPreferredWidth() != widths[i])
				col.setPreferredWidth(widths[i]);
		}						
	}
		
	
	/**
	 * Setups the fundamental view components. 
	 */
	private void setUpGui(){
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.getViewport().add(table);
		scrollPane.setColumnHeader(new JViewport(){
			private static final long serialVersionUID = 1L;

			@Override public Dimension getPreferredSize() {
		        Dimension d = super.getPreferredSize();
		        d.height = 25; //TODO magic number!
		        return d;
			}
		});
		scrollPane.setBorder(EMPTY_BORDER);
		table.setBorder(null);
		add(scrollPane);
	}
	

	/**
	 * Setups the table component of this card grid view.
	 */
	private void setupTable(){
		
		table = new JTable(tableModel);
		table.addFocusListener(internalTableListener);
		table.getSelectionModel().addListSelectionListener(internalTableListener);
		table.setDefaultEditor(Cell.class, new CellEditor());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
		table.setRowSorter(sorter);
		sorter.setModel(tableModel);
		
		table.setDefaultRenderer(Object.class, new CellRenderer());
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		
		lastColumnCount = table.getColumnCount();
		table.setTransferHandler(new TableTransferHandler()); //For custom translation of cell values when copying.
		TableCellRenderer origHeaderRenderer = table.getTableHeader().getDefaultRenderer();
		table.getTableHeader().setDefaultRenderer(
				new HeaderCellDecorator(origHeaderRenderer));
		
	}
	
	/**
	 * Determines the minimal column width for a given column type.
	 * @param type type of column
	 * @return the minimal column width
	 */
	private static int getMinColumnWidth(MtgPricerColumn type){
		if (type == MtgPricerColumn.NAME)
			return NAME_COLUMN_MIN_WIDTH;
		else if (type == MtgPricerColumn.QUANTITY)
			return QUANTITY_COLUMN_MIN_WIDTH;
		else if (type == MtgPricerColumn.RESULT)
			return RESULT_COLUMN_MIN_WIDTH;
		else
			throw new AssertionError();
	}
	
	/**
	 * A transfer handler for converting the selected cells/rows
	 * into meaningful text to be placed in system clipboard.
	 */
	private static class TableTransferHandler extends TransferHandler {

		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip,
				int action) throws IllegalStateException {

			JTable table = (JTable) comp; 
			TableModel model = table.getModel();
		
			//System EOL character.
			String sep = System.lineSeparator();
			//Fall-back value.
			if (sep == null)
				sep = "\n";
		
			StringBuilder sb = new StringBuilder();	
			int firstRow  = table.getSelectedRow();
			int lastRow = table.getSelectedRowCount() + firstRow - 1;
			for (int row = firstRow;  row <= lastRow; row++){
				int firstColumn  = table.getSelectedColumn();
				int lastColumn = table.getSelectedColumnCount() + firstColumn - 1;
				for (int column = firstColumn; column <= lastColumn; column++){
					Cell cell  = (Cell) model.getValueAt(row, column);
					sb.append(cell.getText()).append("\t");
				}
				if (row != lastRow){
					sb.append(sep);
				}
			}
			
			StringSelection sel = new StringSelection(sb.toString());
			
			clip.setContents(sel, null);
		}
			
	}
	
	/**
	 * Decorator to modify rendering of original table cell renderer to
	 * add custom style.
	 */
	private static class HeaderCellDecorator implements TableCellRenderer{
		
		private TableCellRenderer originalRenderer;
		
		public HeaderCellDecorator(TableCellRenderer originalRenderer){
			this.originalRenderer = originalRenderer;
		}
		
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object val, boolean isSelected, boolean hasFocus, int row, int col) {
			
				JLabel lbl = (JLabel) originalRenderer.getTableCellRendererComponent(table,
						val, isSelected, hasFocus, row, col);
				
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
				
				return lbl;
		}
	}
	
	/**
	 * A list selection, table model and focus listener implementation for capturing
	 * events from the internal JTable component and firing it as card grid events to the registered
	 * card grid listeners or doing other internal processing.
	 */
	private class InternalTableListener implements ListSelectionListener, 
		FocusListener, TableModelListener{

		
		@Override
		public void focusGained(FocusEvent e) {
			if (e.getSource() != table)
				throw new AssertionError();
			for (CardGridListener listener : listeners)
				listener.gridFocusGained();
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (e.getSource() != table)
				throw new AssertionError();
			for (CardGridListener listener : listeners)
				listener.gridFocusLost();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			firstSelectedRow = e.getFirstIndex();
			lastSelectedRow = e.getLastIndex();
			
			if (e.getSource() != table.getSelectionModel())
				throw new AssertionError();
			
			if (e.getValueIsAdjusting())
				return; //We are interested in finished selection.
			
			for (CardGridListener listener : listeners)
				listener.gridSelectionChanged(table.getSelectedRows());
		}
		
		@Override
		public void tableChanged(TableModelEvent e) {
			
			if (table.getRowCount() > 0){
				//Prevent selected of rows which are no longer in table.
				firstSelectedRow = Math.min(firstSelectedRow, table.getRowCount()-1);
				lastSelectedRow = Math.min(lastSelectedRow, table.getRowCount()-1);
				
				table.setRowSelectionInterval(firstSelectedRow, lastSelectedRow);
			}
			
			/*The only place where we receive events from changes in model
			 * so we need to track the changes in  column count this way.
			 */
			if (tableModel.getColumnCount() != lastColumnCount){
				System.out.println("The table column count changed from " + 
						lastColumnCount + " to  " + tableModel.getColumnCount());
				//Set the minimal column widths for all columns.
				TableColumnModel columnModel = table.getColumnModel();
				for (int i = 0; i < tableModel.getColumnCount(); i++) {
					MtgPricerColumn column = tableModel.getColumnType(i);
					int minWidth = getMinColumnWidth(column);
					columnModel.getColumn(i).setMinWidth(minWidth);
				}
				
				lastColumnCount = tableModel.getColumnCount();
			}
			setColumnsAutoWidth();
		}
		
	}
}
