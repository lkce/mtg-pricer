package sk.lkce.mtgp.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import sk.lkce.mtgp.tablemodel.Cell;


/**
 * A cell editor used in {@link CardGrid} table.
 * It supports editing of card name - with text field
 * or card quantity - with spinner.
 */
@SuppressWarnings("serial")
public class CellEditor extends AbstractCellEditor 
	implements TableCellEditor{
	
	private final JSpinner spinner = new QuantitySpinner();
	private final JTextField textField = new JTextField();
	private Component editor; 
	private String originalValue;
	
	@Override
	public Object getCellEditorValue() {
		if (editor instanceof JSpinner)
			return ((JSpinner) editor).getValue();
		
		if (editor instanceof JTextField)
			return ((JTextField) editor).getText();
		
		return spinner.getValue();
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
	                        int row, int column) {
		Cell cell = (Cell) value;
		String val = cell.getText();
		originalValue = val;
		
		
		if (cell.getType() == Cell.Type.INTEGER){
			spinner.setValue(Integer.parseInt(val));
			editor = spinner;
		}else{
			textField.setText(val);
			editor = textField;
		}
		
		
		return editor;
	}
	
	
	
	
	@Override
	public boolean isCellEditable(EventObject e) {
	    if (e instanceof MouseEvent) {
            return ((MouseEvent)e).getClickCount() >= 2;
        }
		return true;
	}


	@Override
	public boolean stopCellEditing() {
		if (getCellEditorValue().equals(originalValue))
			cancelCellEditing();
		
		
		return super.stopCellEditing();
	}
	
	
	
}