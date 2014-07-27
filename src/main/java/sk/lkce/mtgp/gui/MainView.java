package sk.lkce.mtgp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sk.lkce.mtgp.cardsearch.SearchExecutor;
import sk.lkce.mtgp.gui.Controller.Phase;
import sk.lkce.mtgp.gui.Controller.UserAction;

/**
 * A main view class which aggregates all view components
 * and provides unified view interface for the classes
 * which contain the application logic.<br>
 * Presentation logic
 * is also included here.
 * 
 * The view consists of 4 main parts:
 * 
 * <ul>
 * 	<li>'card grid' - a table with card list and search results for each card</li>
 * 	<li>'finders pane' - a column on the left which shows the list of 
 *    possible card finders for the search and the search progress for each card finder</li>
 * 	<li>A toolbar with button which trigger user actions </li>
 * 	<li>A menu bar in the top part of the window</li>
 *
 * _____________________________________________________
 * | Menu bar                                          | 	
 * |---------------------------------------------------|
 * | Tool bar                                          |
 * |---------------------------------------------------|
 * |             |                                     |
 * |             |                                     |                                
 * |             |                                     |
 * |             |    Cards grid                       |
 * |Finders pane |                                     |
 * |             |                                     |
 * |             |                                     |
 * |_____________|_____________________________________|
 * 
 *
 */
public class MainView {
	
	
	private final static Color BORDER_COLOR = new Color(190,190,190);
	private static final Border LINE_BORDER = BorderFactory.createLineBorder(BORDER_COLOR);
	private static final Border PADDING_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);
	private static final Border ETCHED_BORDER = BorderFactory.createCompoundBorder(PADDING_BORDER, LINE_BORDER);
	
	//TODO what with these?
	private static final int HEIGHT = 500;
 	private static final int WIDTH = 850;

 	private JPanel windowPane;
	private JPanel tablePane;
	private CardGrid cardGrid;
	private CardFindersPane findersPane;
	
	private JTextField addTextField;
	private JSpinner addSpinner;
	private JToolBar toolBar;
	private final JFrame window;
	private static final String TITLE = "Mtg Pricer";
	private JDialog aboutDialog;
	private Controller controller;
	
	/**
	 * Constructs a new main view build around the specified controller.
	 * @param controller the main application controller
	 */
	public MainView(Controller controller){
		window = new JFrame();
		window.setTitle(TITLE);
		window.setIconImage(ResourceLoader.ICON_APP.getImage());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		windowPane = new JPanel(new BorderLayout());
		windowPane.setBorder(PADDING_BORDER);
		window.add(windowPane);
		this.controller = controller;
		setupGui();
	}

	/**
	 * Makes the whole gui of the application visible.
	 */
	public void show(){
		window.setSize(WIDTH, HEIGHT);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	/**
	 * Invoked when the search has started to let the view
	 * react on entering the setting phase. 
	 * Apart from preparing the whole view for the search state it also 
	 * disables the control elements which should not
	 * be enabled during the searching (or also during the final) phase.
	 * 
	 * @param executor the search executor used in the starting search
	 */
	public void searchStarted(SearchExecutor executor){
		findersPane.showSearchProgress(executor);
		addSpinner.setEnabled(false);
		addTextField.setEnabled(false);
	}
	
	/**
	 * Invoked when the application has entered the first - setting phase.
	 * Changes the view look and enabled state of the controls accordingly.
	 */
	public void newPricing(){
		addSpinner.setEnabled(true);
		addTextField.setEnabled(true);
		setNewFindersPane();
		findersPane.showFinderSettings();
	}
	
	/**
	 * Clears the actual text value in the card name text field.
	 */
	public void clearAddCardTextField(){
		addTextField.setText("");
	}
	
	/**
	 * Invoked when the user has stopped the search. Commands
	 * the view to have 'search is being stopped' look.
	 */
	public void stopSearchIssued(){
		window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		findersPane.displayStoppingSearch();
	}

	/**
	 * Invoked when the search has been stopped. Logically following the
	 * 'search is being stopped phase' and it commands the view
	 * to change 'search is being stopped ' into 'search has been stopped' look. 
	 * Also implies entering the final ( {@link Phase#PRICING_FINISHED} ) phase.
	 */
	public void searchStopped(){
		window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		findersPane.displaySearchStopped();
		cardGrid.setRowSelectionAllowed(true);
	}

	/**
	 * Invoked when the search has successfully finished without stop from the user and
	 *  the application has entered the final ( {@link Phase#PRICING_FINISHED} ) phase.
	 */
	public void searchFinished(){
		findersPane.displaySearchFinished();
		cardGrid.setRowSelectionAllowed(true);
	}
	
	
	/**
	 * Shows modal 'About' dialog with the information abou
	 * the application and the author.
	 */
	public void showAbout() {
		if (aboutDialog == null)
			aboutDialog = new AboutDialog(window);
		aboutDialog.setVisible(true);
    }

	/**
	 * Reports an error message  to the user.
	 * @param text the description of the error
	 */
	public void reportError(String text){
		JOptionPane.showMessageDialog(window,
				text ,"Sorry...",JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Commands this view to interactively demand confirmation (yes/no)
	 * for a given question from the user and returns the answer.
	 * 
	 * @param title the title of the confirmation request
	 * @param text the text of the confirmation request
	 * @return <code>true</code> if the user has confirmed  
	 * 	<code>false</code> if the user has declined
	 */
	public boolean askForConfirmation(String title, String text){
		int response = JOptionPane.showConfirmDialog(window, text, title, JOptionPane.YES_NO_OPTION);
		return (response == JOptionPane.YES_OPTION);
	}
	
	/**
	 * Setups all the main GUI elements.
	 */
	private void setupGui(){
		
		tablePane = new JPanel(new BorderLayout());
		tablePane.setBorder(ETCHED_BORDER);
		cardGrid = new CardGrid(controller.getTableModel());
		cardGrid.addGridListener(controller);
		cardGrid.registerAction(controller.getAction(UserAction.REMOVE_CARD));
		tablePane.add(cardGrid);
		
		toolBar = createToolBar();
		windowPane.add(toolBar, BorderLayout.NORTH);
		
		windowPane.add(tablePane, BorderLayout.CENTER);
		window.setJMenuBar(createMenuBar());
		
	}
	
	/**
	 * Creates a new finders pane component.
	 */
	private void setNewFindersPane(){
		if (findersPane != null)
			windowPane.remove(findersPane);
		findersPane = new CardFindersPane(controller);
		findersPane.setBorder(ETCHED_BORDER);
		windowPane.add(findersPane, BorderLayout.WEST);
		windowPane.revalidate();
	}
	
	/**
	 * Creates a new tool bar for this view.
	 */
	private JToolBar createToolBar(){
		JToolBar tb = new JToolBar();
		
		tb.setFocusable(false);
		tb.add(controller.getAction(UserAction.IMPORT_CARDS));
		tb.addSeparator(new Dimension(10,20));

		addTextField = new JTextField(10);
		addTextField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "submit");
		addTextField.getActionMap().put("submit", controller.getAction(UserAction.ADD_CARD));
		
		addTextField.setMaximumSize(addTextField.getPreferredSize());
		addTextField.getDocument().addDocumentListener(new DocumentListener() {
			
			private void informController(){
				controller.cardTextFieldValueChanged(addTextField.getText());
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				informController();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				informController();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				informController();
			}
		});
		
		
		tb.add(addTextField);
		
		addSpinner = new QuantitySpinner();
		addSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				Integer value = (Integer) addSpinner.getValue();
				controller.quantitySpinnerValueChanged(value);
			}
		});

		tb.add(addSpinner);
		tb.add(controller.getAction(UserAction.ADD_CARD));
		tb.add(controller.getAction(UserAction.REMOVE_CARD));
		
		tb.addSeparator(new Dimension(10,20));
		tb.add(controller.getAction(UserAction.START_SEARCH));
		tb.add(controller.getAction(UserAction.STOP_SEARCH));
		tb.addSeparator(new Dimension(10,20));
		tb.add(controller.getAction(UserAction.OPEN_IN_BROWSER));
		
		//Make all buttons unfocusable.
		for (Component c : tb.getComponents())
			if (c instanceof AbstractButton)
				((AbstractButton)c).setFocusable(false);
		tb.setBorder(ETCHED_BORDER);
		
		return tb;
	}
	
	/**
	 * Creates a new menu bar for this view.
	 */
	private JMenuBar createMenuBar(){
		JMenuBar  menuBar = new JMenuBar();
		JMenu menu = new JMenu();

		menu = new JMenu("Search");
		menu.setMnemonic(KeyEvent.VK_S);
		
		JMenuItem mi = new JMenuItem(controller.getAction(UserAction.NEW_SEARCH));
		menu.add(mi);
		mi = new JMenuItem(controller.getAction(UserAction.START_SEARCH));
		mi.setIcon(null);
		menu.add(mi);
		mi = new JMenuItem(controller.getAction(UserAction.OPEN_IN_BROWSER));
		mi.setIcon(null);
		menu.add(mi);
		menuBar.add(menu);
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);

		mi = new JMenuItem(controller.getAction(UserAction.IMPORT_CARDS));
		mi.setIcon(null);
		menu.add(mi);
		mi = new JMenuItem(controller.getAction(UserAction.REMOVE_CARD));
		mi.setIcon(null);
		menu.add(mi);
		menuBar.add(menu);
		
		menu = new JMenu("Export");
		menu.setMnemonic(KeyEvent.VK_X);
		mi = new JMenuItem(controller.getAction(UserAction.EXPORT_TO_CSV));
		menu.add(mi);
		mi = new JMenuItem(controller.getAction(UserAction.EXPORT_TO_TXT));
		menu.add(mi);
		menuBar.add(menu);
		menu= new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		mi = new JMenuItem("About MtGPricer");
		mi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showAbout();
				
			}
		});
		menu.add(mi);
		menuBar.add(menu);
		
		return menuBar;
	}
	
}
