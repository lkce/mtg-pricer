package sk.lkce.mtgp.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import sk.lkce.mtgp.tablemodel.Cell;

/**
 * A cell renderer used for all cells in the {@link CardGrid} table.
 * It renders the cell view according to its type.
 */
@SuppressWarnings("serial")
class CellRenderer extends DefaultTableCellRenderer{
	
	private final Border padding = BorderFactory.createEmptyBorder(2, 3, 2, 2);
	private Color originalColor;
	private final Color notFoundColor = Color.RED; 
	
	/**
	 * Creates a new card grid cell renderer.
	 */
	public CellRenderer(){
		originalColor = getForeground();
	}
	
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object val, boolean isSelected, boolean hasFocus, int row, int col) {
		
		
		Cell cell = (Cell) val;
		JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, col);
		
		
		if (cell == Cell.NOT_FOUND_CELL){
			lbl.setForeground(notFoundColor);
		}else
			lbl.setForeground(originalColor);

		lbl.setBorder(padding);
		lbl.setText(cell.getText());
		lbl.setHorizontalAlignment(getAllignment(cell.getType()));

		return lbl;
	}
	
	/**
	 * Determines alignment for a cell type.
	 */
	private int getAllignment(Cell.Type type){
		if (type == Cell.Type.INTEGER
				|| type == Cell.Type.PRICE)
			return SwingConstants.RIGHT;
		else
			return SwingConstants.LEFT;
	}
	
}