package sk.lkce.mtgp.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import sk.lkce.mtgp.cardsearch.CardFinder;
import sk.lkce.mtgp.cardsearch.CardFinderFactory;
import sk.lkce.mtgp.cardsearch.CardParser;
import sk.lkce.mtgp.cardsearch.CardSearchResultSet;
import sk.lkce.mtgp.cardsearch.SearchExecutor;
import sk.lkce.mtgp.cardsearch.SearchObserver;
import sk.lkce.mtgp.domain.Card;
import sk.lkce.mtgp.domain.CardResult;
import sk.lkce.mtgp.domain.PricingSettings;
import sk.lkce.mtgp.tablemodel.MtgPricerTableModel;
import sk.lkce.mtgp.tablemodel.ReportCreator;

/**
 * An application controller.It manages the whole flow and contains most of the logic and
 * contains actions implementation as inner classes.
 * <br>
 * The whole process of finding the prices of the card on the web is referred to 
 * as card pricing. The pricing process involves three main states (see {@link Phase} ):
 * <br> 
 * <ol>
 * 	<li>Setting phase where the list of card is loaded and the card finders (objects responsible
 * for retrieving the price information from vendors) are chosen.</li>
 *  <li>Actual pricing process which consists of downloading and parsing the card information</li>
 *  <li>Final phase where results are presented</li>
 * </ol>
 * 
 * <br>
 * 
 */
public class Controller implements SearchObserver, CardGridListener {

	/**
	 * A user action type.
	 */
	public enum UserAction {
		NEW_SEARCH, ADD_CARD, REMOVE_CARD, IMPORT_CARDS, EXPORT_TO_CSV, 
		EXPORT_TO_TXT, OPEN_IN_BROWSER, START_SEARCH, STOP_SEARCH
	}

	/**
	 * A phase of the pricing process.
	 */
	public enum Phase {
		/**
		 * Setting phase where the card finders are selected and 
		 * card list is created by the user.
		 */
		SETTING,
		/**
		 * Actual process of pricing.
		 */
		SEARCHING, 
		
		/**
		 * A phase where the pricing has finished and results are presented.
		 */
		PRICING_FINISHED
	}

	private Map<UserAction, AbstractAction> actionMap = new HashMap<>();
	private SearchExecutor searchExecutor;
	private PricingSettings pricingSettings;
	private List<CardFinder> finders;
	private MtgPricerTableModel tableModel;
	private Phase currentPhase;
	private MainView mainView;
	private String cardNameTextFieldValue;
	private int quantitySpinnerValue = 1; // Default (and also minimal)spinner
											// value
	private List<Card> selectedCards = new ArrayList<>();

	/**
	 * Constructs a controller.
	 */
	public Controller() {
		createActions();
		finders = CardFinderFactory.allCardFinders();
		tableModel = new MtgPricerTableModel(this);
		mainView = new MainView(this);
		mainView.show();
	}

	
	/**
	 * Makes the application enter the initial {@link Setting} phase.
	 * All settings and results are discarded and everything is reset
	 * and set ready for user input.
	 */
	public void newPricing() {
		currentPhase = Phase.SETTING;
		pricingSettings = new PricingSettings();
		for (CardFinder finder : finders)
			// Add all card finders as default
			pricingSettings.addFinder(finder);
		tableModel.newPricing(pricingSettings);
		mainView.newPricing();
		setDefaultActionAvailability();
		tableModel.fireTableStructureChanged();
	}

	/**
	 * Returns the pricing settings object.
	 * @return the current pricing settings
	 */
	public PricingSettings getPricingSettings() {
		return pricingSettings;
	}

	/**
	 * Returns an application table model.
	 * @return the current table model
	 */
	public MtgPricerTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * Enables or disables  the specified {@link CardFinder} for pricing. Possible only
	 * during {@link Phase#SETTING} phase.
	 * @param finder  the finder to be enabled/disabled
	 * @param enabled <code>true</code> if the card finder should be enabled, <code>false</code> if disabled
	 * @throws IllegalStateException if an attempt is made to use these method outside the settings phase.
	 */
	public void setFinderEnabled(CardFinder finder, boolean enabled) {
		if (currentPhase != Phase.SETTING)
			throw new IllegalStateException("Changing pricing settings " + "is only possible during the "
					+ Phase.SETTING + " phase.");

		if (enabled && !pricingSettings.getFinders().contains(finder))
			pricingSettings.addFinder(finder);

		if (!enabled && pricingSettings.getFinders().contains(finder))
			pricingSettings.removeFinder(finder);
		actionMap.get(UserAction.START_SEARCH).setEnabled((pricingSettings.getFinders().size() > 0));

	}

	/**
	 * Returns all possible card finders regardless whether they are selected
	 * for the search or not.
	 * @return an unmodifiable list of all card finders
	 */
	public Collection<CardFinder> getCardFinders() {
		return Collections.unmodifiableList(finders);
	}

	/**
	 * Invoked on controller (from the presentation layer) if the text field value has been changed.
	 * TOOD: Maybe reduce coupling and define listener interface for event from view.
	 * @param newText a new text of the text field 
	 */
	public void cardTextFieldValueChanged(String newText) {
		if (newText.isEmpty()) {
			cardNameTextFieldValue = null;
			disableAction(UserAction.ADD_CARD);
		} else {
			cardNameTextFieldValue = newText;
			enableAction(UserAction.ADD_CARD);
		}
	}

	/**
	 * Invoked on controller (from the presentation layer) if the card quantity spinner value has been changed.
	 * TOOD: Maybe reduce coupling and define listener interface for event from view.
	 * @param newValue a new value of card quantity spinner component
	 */
	public void quantitySpinnerValueChanged(int newValue) {
		quantitySpinnerValue = newValue;
	}

	/**
	 * Orders this controller to display an error message to the user.
	 * @param txt the text of the message
	 */
	public void displayErroMessage(String txt) {
		mainView.reportError(txt);
	}

	@Override
	public void searchStarted(int numberOfCards) {

	}

	@Override
	public void cardSearchStarted(Card card, CardFinder finder) {
		// Empty
	}

	@Override
	public void searchThreadFailed(CardFinder finder, Throwable t) {
		// TODO Auto-generated method stub
	}

	@Override
	public void cardSearchFinished(Card card, CardResult result, CardFinder finder) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				tableModel.fireTableRowsUpdated(0, Integer.MAX_VALUE);
			}
		});
	}

	@Override
	public void searchThreadFinished(CardFinder finder, CardSearchResultSet results) {
		// TODO Auto-generated method stub

	}

	@Override
	public void searchingFinished(final boolean interrupted) {
		currentPhase = Phase.PRICING_FINISHED;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (interrupted)
					mainView.searchStopped(); // In case the search was stopped
												// by user and the view was set
												// to busy state
				else
					mainView.searchFinished();
				disableAction(UserAction.STOP_SEARCH);
				enableAction(UserAction.NEW_SEARCH);
			}
		});
	}

	/**
	 * Adds a card to the list of cards for which the price should be find. If the 
	 * card is already in the card list a pop-up dialog is shown and ask user if
	 * instead of replacing the old quantity of the card, the new quantity should be
	 * the sum of the old and the new quantity (<b>quantity</b>).
	 * 
	 * @param card the card to be added
	 * @param quantity the quantity of the card
	 */
	private void addCardLeniently(Card card, int quantity) {

		if (pricingSettings.getCards().contains(card)) {
			int oldQ = pricingSettings.getQuantity(card);
			String msg = "<html>Card <b><i> " + card.getName() + "</i></b> is already"
					+ " present in the deck (quantity: " + oldQ + ")." + " Do you really want to add "
					+ quantity + " pieces of this card to the deck?";

			String title = "Duplicate detected";
			if (mainView.askForConfirmation(title, msg)) {
				pricingSettings.setNewQuantity(card, oldQ + quantity);
			}

		} else
			pricingSettings.addCard(card, quantity);
	}

	@Override
	public void gridFocusLost() {
		// Empty.
	}

	@Override
	public void gridFocusGained() {
		// Empty.
	}

	@Override
	public void gridSelectionChanged(int[] selectedRows) {

		selectedCards.clear();
		for (int rowIndex : selectedRows)
			selectedCards.add(tableModel.getCardAt(rowIndex));

		if (selectedRows.length == 0) {
			disableAction(UserAction.REMOVE_CARD);
			disableAction(UserAction.OPEN_IN_BROWSER);
		} else {
			enableAction(UserAction.REMOVE_CARD);
			enableAction(UserAction.OPEN_IN_BROWSER);
		}
	}

	/**
	 * Creates all actions and puts them to the action map.
	 */
	private void createActions() {

		AbstractAction action = new StartSearchAction();
		actionMap.put(UserAction.START_SEARCH, action);
		action = new StopSearchAction();
		actionMap.put(UserAction.STOP_SEARCH, action);
		action = new ImportCardsAction();
		actionMap.put(UserAction.IMPORT_CARDS, action);
		action = new AddCardAction();
		actionMap.put(UserAction.ADD_CARD, action);
		action = new RemoveCardAction();
		actionMap.put(UserAction.REMOVE_CARD, action);
		action = new ExportTableCsvAction();
		actionMap.put(UserAction.EXPORT_TO_CSV, action);
		action = new ExportTableTxtAction();
		actionMap.put(UserAction.EXPORT_TO_TXT, action);
		action = new NewPricingAction();
		actionMap.put(UserAction.NEW_SEARCH, action);
		action = new SearchInBrowserAction();
		actionMap.put(UserAction.OPEN_IN_BROWSER, action);
		setDefaultActionAvailability();
	}

	/**
	 * Primitive implementation. For better code readability & debugging.
	 */
	private void enableAction(UserAction action) {
		actionMap.get(action).setEnabled(true);
	}

	/**
	 * Primitive implementation. For better code readability & debugging.
	 */
	private void disableAction(UserAction action) {
		actionMap.get(action).setEnabled(false);
	}

	/**
	 * Sets the default or starting actions availability as it
	 * should be in the first (setting) phase.
	 */
	private void setDefaultActionAvailability() {
		enableAction(UserAction.IMPORT_CARDS);
		disableAction(UserAction.REMOVE_CARD);
		disableAction(UserAction.ADD_CARD);
		disableAction(UserAction.OPEN_IN_BROWSER);
		disableAction(UserAction.START_SEARCH);
		disableAction(UserAction.STOP_SEARCH);
		disableAction(UserAction.REMOVE_CARD);
	}

	/**
	 * Returns the action implementation for the specified
	 * user action type.
	 * @param action the action type
	 * @return the action for the specified action type
	 */
	public Action getAction(UserAction action) {
		return actionMap.get(action);
	}

	/**
	 * A user interface action which starts the new pricing.
	 */
	@SuppressWarnings("serial")
	private class NewPricingAction extends AbstractAction {

		NewPricingAction() {
			super("New search");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			assert currentPhase != Phase.SEARCHING;
			newPricing();
		}

	}

	/**
	 *  A user interface action which take the current value from the card name text
	 *  field, creates a card based on that value and adds it leniently to the card list.
	 *
	 */
	@SuppressWarnings("serial")
	private class AddCardAction extends AbstractAction {

		AddCardAction() {
			super("Add", ResourceLoader.ICON_ADD);
			putValue(Action.SHORT_DESCRIPTION, "Add a new card row");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (cardNameTextFieldValue == null)
				throw new AssertionError();
			Card card = new Card(cardNameTextFieldValue);
			addCardLeniently(card, quantitySpinnerValue);
			tableModel.fireTableDataChanged();
			mainView.clearAddCardTextField();
			cardNameTextFieldValue = null;

			if (pricingSettings.getCards().size() > 0)
				enableAction(UserAction.START_SEARCH); // Enable start search if
														// we added some cards
		}

		/**
    	 * Intercepts the call and inspects if the action can really be enabled.
		 * The add card action can be enabled only in the setting phase.
		 */
		@Override
		public void setEnabled(boolean newValue) {
			if (newValue) {
				if (currentPhase != Phase.SETTING)
					return;
			}
			super.setEnabled(newValue);
		}

	}

	/**
	 * A user interface action which removes the cards selected in the table from the card list.
	 *
	 */
	@SuppressWarnings("serial")
	private class RemoveCardAction extends AbstractAction {

		RemoveCardAction() {
			super("Delete", ResourceLoader.ICON_REMOVE);
			putValue(Action.SHORT_DESCRIPTION, "Remove selected card row(s)");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedCards.size() < 1)
				throw new AssertionError();
			for (Card card : selectedCards)
				pricingSettings.removeCard(card);

			tableModel.fireTableStructureChanged();

			if (pricingSettings.getCards().size() < 1) // Disable start search
														// if there are no cards
														// left
				disableAction(UserAction.START_SEARCH);
		}
		
		/**
		 * The remove card action cannot be enabled during the searching phase.
		 */
		@Override
		public void setEnabled(boolean newValue) {
			if (newValue) {
				if (currentPhase == Phase.SEARCHING)
					return;
			}
			super.setEnabled(newValue);
		}

	}

	/**
	 * A user interface action which shows a file selection dialog and after submitting
	 * the file, imports the card names and quantity from this file to the card list.
	 */
	private class ImportCardsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		ImportCardsAction() {
			super("Import card list", ResourceLoader.ICON_IMPORT);
			putValue(Action.SHORT_DESCRIPTION, "Append cards from the .txt file");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(null);
			File f = fileChooser.getSelectedFile();

			if (f == null)
				return;

			Map<Card, Integer> result = null;
			try {
				result = CardParser.parseFromFile(f);
			} catch (IOException e) {
				mainView.reportError("An expception ocurred while " + "attempting to read from the file\n "
						+ f.getAbsolutePath() + "\n\n" + e.getMessage());
				e.printStackTrace();
			} catch (ParseException e) {
				mainView.reportError(e.getMessage());
				e.printStackTrace();
			}

			if (result == null)
				return;

			for (Card c : result.keySet())
				addCardLeniently(c, result.get(c));
			tableModel.fireTableStructureChanged();

			if (pricingSettings.getCards().size() > 0) // Enable start search if
														// we added some cards
				enableAction(UserAction.START_SEARCH);

		}
	}

	/**
	 * A user interface action which exports the current content of the table view
	 * into a csv file.
	 */
	private class ExportTableCsvAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		ExportTableCsvAction() {
			super("Export to .csv");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String name = "exported-deck_" + sdf.format(new Date()) + ".csv";
			chooser.setSelectedFile(new File(name));
			chooser.showSaveDialog(null);
			File f = chooser.getSelectedFile();
			if (f == null)
				return;

			ReportCreator report = new ReportCreator(tableModel);
			try {
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(report.createCSVReport(","));
				bw.close();
			} catch (IOException ex) {
				mainView.reportError("An I/O exception occurred while writing to file\n" + f.getName()
						+ "\n\n" + ex.getMessage());
				ex.printStackTrace();
			}

		}

	}

	/**
	 * A user interface naction which exports the current content of the table view
	 * into a csv file.
	 */
	private class ExportTableTxtAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		ExportTableTxtAction() {
			super("Export to .txt");
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			JFileChooser chooser = new JFileChooser();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String name = "exported-deck_" + sdf.format(new Date()) + ".txt";
			chooser.setSelectedFile(new File(name));
			chooser.showSaveDialog(null);
			File f = chooser.getSelectedFile();
			if (f == null)
				return;

			ReportCreator report = new ReportCreator(tableModel);
			try {
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(report.generateTxtReport());
				bw.close();
			} catch (IOException ex) {
				mainView.reportError("An I/O exception occurred while writing to file\n" + f.getName()
						+ "\n\n" + ex.getMessage());
				ex.printStackTrace();
			}

		}

	}

	/**
	 * A user interface action which starts the search which makes the
	 * application enter the searching phase.
	 */
	@SuppressWarnings("serial")
	private class StartSearchAction extends AbstractAction {

		StartSearchAction() {
			super("Start search", ResourceLoader.ICON_GO);
			putValue(Action.SHORT_DESCRIPTION, "Run card prices search");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			searchExecutor = new SearchExecutor(pricingSettings.getCards(), pricingSettings.getFinders());
			searchExecutor.addSearchObserver(Controller.this);
			currentPhase = Phase.SEARCHING;
			mainView.searchStarted(searchExecutor);
			searchExecutor.startSearch();
			tableModel.searchStarted(searchExecutor.getResultsStorage());
			enableAction(UserAction.STOP_SEARCH);
			disableAction(UserAction.START_SEARCH);
			disableAction(UserAction.ADD_CARD);
			disableAction(UserAction.REMOVE_CARD);
			disableAction(UserAction.IMPORT_CARDS);
			disableAction(UserAction.NEW_SEARCH);

		}

		/**
		 * Intercepts the call and inspects if the action can really be enabled.
		 */
		@Override
		public void setEnabled(boolean newValue) {
			if (newValue) {
				// There must be at least 1 card and card pricer selected.
				if (pricingSettings.getCards().size() == 0 || pricingSettings.getFinders().size() == 0)
					return;
			}
			super.setEnabled(newValue);
		}

	}

	/**
	 * A user interface action which stops the ongoing search.
	 */
	@SuppressWarnings("serial")
	private class StopSearchAction extends AbstractAction {

		StopSearchAction() {
			super("Stop search", ResourceLoader.ICON_STOP);
			putValue(Action.SHORT_DESCRIPTION, "Stop the running search");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			searchExecutor.stopSearch();
			mainView.stopSearchIssued(); // Active state will be set in observer
											// method when the search has been
											// reported to finish
			setEnabled(false);
		}
	}


	/**
	 * A user interface action which issues the request for the card search
	 * in the browser for each selected card and for each selected card finder.
	 * This way the web pages of vendors associated with the selected card finders
	 * are opened which are response to the card search request encoded in the request URL.
	 */
	@SuppressWarnings("serial")
	private class SearchInBrowserAction extends AbstractAction {

		SearchInBrowserAction() {
			super("Find via browser", ResourceLoader.ICON_BROWSER);
			putValue(Action.SHORT_DESCRIPTION, "Open web pages with search result");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO handle this somewhere. Do not enable the action.
			// assert Desktop.isDesktopSupported();

			Collection<CardFinder> finders = pricingSettings.getFinders();

			for (Card c : selectedCards)
				for (CardFinder f : finders) {
					URL url = f.getURLForCard(c.getName());
					try {
						Desktop.getDesktop().browse(url.toURI());
					} catch (IOException | URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

		}
	}

}
