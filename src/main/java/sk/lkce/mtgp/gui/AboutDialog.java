package sk.lkce.mtgp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * A dialog with the information about the
 * application and the author.
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private static final int HEIGHT = 150;
	private static final int WIDTH = 220;
	
	private static final String TEXT = "<html><b>MtG Pricer<br> v1.0 </b><br><br> Simple tool for searching for "
			+ "the Magic the Gathering card prices<br><br>"
			+ "<i>LC 2014</i></html>";
	
	/**
	 * Constructs an about dialog with a given parent frame.
	 * If parent is null, a shared, hidden
	 * frame will be set as the parent of the dialog. 
	 * 
	 * @param parent the frame from which the dialog is displayed
	 */
	public AboutDialog(JFrame parent) {
		super(parent,true);
		setUndecorated(true);
		setSize(WIDTH,HEIGHT);
		setLocationRelativeTo(parent);
		
		//Set up content.
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new MigLayout());
		centerPanel.add(new JLabel(TEXT));
		
		JPanel panelBottom = new JPanel();
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));
		contentPanel.add(centerPanel,BorderLayout.CENTER);
		contentPanel.add(panelBottom, BorderLayout.SOUTH);
		
		//Set up 'Close' button.
		JButton button = new JButton("Close");
		button.setFocusable(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		panelBottom.add(button);
	
		add(contentPanel);
	}
}
