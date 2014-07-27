package sk.lkce.mtgp.gui;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

/**
 * An extension of the Swing spinner 
 * which implements stricter validation.
 */
public class QuantitySpinner extends JSpinner {
	
	private static final long serialVersionUID = 1L;

	public QuantitySpinner(){
		setModel(new SpinnerNumberModel(1,1,99,1));
		setEditor(new JSpinner.NumberEditor(this,"##"));
		JFormattedTextField txt = ((JSpinner.NumberEditor) getEditor()).getTextField();
		((NumberFormatter) txt.getFormatter()).setAllowsInvalid(false);
		setMaximumSize(getPreferredSize());
	}

}

