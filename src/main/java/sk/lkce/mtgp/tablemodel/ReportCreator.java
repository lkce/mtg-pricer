package sk.lkce.mtgp.tablemodel;

import java.util.HashMap;
import java.util.Map;

import sk.lkce.mtgp.gui.CardGrid;
import sk.lkce.mtgp.tablemodel.MtgPricerTableModel.MtgPricerColumn;

/**
 * A generator of the simple text reports in various formats which
 * are based on the contents of the mtg pricer table. 
 * Used for export functionality.
 */
public class ReportCreator {

	private MtgPricerTableModel tableModel;

	/**
	 * Constructs a report creator bound to the specified table model.
	 * @param tableModel the table model containing the data
	 */
	public ReportCreator(MtgPricerTableModel tableModel) {
		this.tableModel = tableModel;
	}

	/**
	 * Creates a simple text report with ASCII table which 
	 * shows the content of the card grid/table model
	 * @return ASCII table with information from the card grid/table model
	 */
	public String generateTxtReport() {

		
		
		int rowCount = tableModel.getRowCount();
		int colCount = tableModel.getColumnCount();

		// Find out maximum widths.
		Map<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();
		for (int column = 0; column < colCount; column++) {
			int maxWidth = tableModel.getColumnName(column).length();
			for (int row = 0; row < rowCount; row++) {
				Cell val = (Cell) tableModel.getValueAt(row, column);
				maxWidth = Math.max(maxWidth, val.getText().length());
			}
			columnWidths.put(column, maxWidth);
		}
		
		//Ensure the width of the first column is at least the size of the last row label.
		String totalStr = "Total price";
		if (columnWidths.get(0) < totalStr.length())
			columnWidths.put(0, totalStr.length());
		

		StringBuilder sb = new StringBuilder();
		// Write headers.
		for (int i = 0; i < colCount; i++) {
			String text = alignLeft(tableModel.getColumnName(i),
					columnWidths.get(i));
			sb.append(text);
			if (i - 1 < colCount)
				sb.append(" | ");
		}
		sb.append("\n");
		// Separating line.
		for (int i = 0; i < colCount; i++) {
			for (int j = 0; j < columnWidths.get(i); j++)
				sb.append("-");
			if (i - 1 < colCount)
				sb.append(" | ");
		}
		sb.append("\n");

		// Write rest of values.
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < colCount; j++) {
				sb.append(getPaddedValue(i, j, columnWidths.get(j)));

				if (j - 1 < colCount)
					sb.append(" | ");
			}
			sb.append("\n");
		}


		// Separating line.
		for (int i = 0; i < colCount; i++) {
			for (int j = 0; j < columnWidths.get(i); j++)
				sb.append("-");
			if (i - 1 < colCount)
				sb.append(" | ");
		}
		sb.append("\n");
		
		//TODO add totals
		
		return sb.toString();
	}


	/**
	 * Returns a string which contains the contents of the table model  - {@link MtgPricerTableModel}/{@link CardGrid}. 
	 * The columns are separated by the specified separator.
	 * @param separator the separating character
	 * @return the string representation of the table contents with columns separated by <code>separator</code>
	 */
	public String createCSVReport(String separator) {

		StringBuilder sb = new StringBuilder();

		//Headers
		for (int i = 0; i < tableModel.getColumnCount(); i++)
			sb.append(tableModel.getColumnName(i) + separator);

		sb.append("\n");

		//Body
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			for (int j = 0; j < tableModel.getColumnCount(); j++) {
				Cell cell  = (Cell) tableModel.getValueAt(i, j);
				sb.append(cell.getText());
				sb.append(separator);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Returns a string for a given row and column index in the table model
	 * which is padded either left or right based on the column type and with
	 * the specified padding width.
	 * @param row the row index
	 * @param column the column index
	 * @return the padded string at the row and column index
	 */
	private String getPaddedValue(int row, int column, int width) {
		MtgPricerColumn col = tableModel.getColumnType(column);
		Cell val = (Cell) tableModel.getValueAt(row, column);
		String res = val.getText();

		if (col == MtgPricerColumn.QUANTITY || col == MtgPricerColumn.RESULT)
			res = alignRight(res, width);
		else
			res = alignLeft(res, width);

		return res;
	}

	/**
	 * Pads the right side of a string.
	 * @param s the string to be padded
	 * @param width the width of padding
	 * @return the padded string
	 */
	private String alignLeft(String s, int width) {
		return String.format("%-" + width + "s", s);
	}

	/**
	 * Pads the left side of a string.
	 * @param s the string to be padded
	 * @param width the width of the padding
	 * @return the padded string
	 */
	private String alignRight(String s, int width) {
		return String.format("%" + width + "s", s);
	}
}
