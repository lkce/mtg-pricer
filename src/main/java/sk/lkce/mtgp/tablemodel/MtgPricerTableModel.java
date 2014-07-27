package sk.lkce.mtgp.tablemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import sk.lkce.mtgp.cardsearch.CardFinder;
import sk.lkce.mtgp.cardsearch.CardSearchResultSet;
import sk.lkce.mtgp.domain.Card;
import sk.lkce.mtgp.domain.CardResult;
import sk.lkce.mtgp.domain.PricingSettings;
import sk.lkce.mtgp.gui.CardGrid;
import sk.lkce.mtgp.gui.Controller;
import sk.lkce.mtgp.gui.Controller.Phase;

/**
 * A table model for  {@link CardGrid}.
 * The table has the following columns:
 * <ol>
 * 	<li>Card name column</li>
 * 	<li>Card quantity column</li>
 * 	<li>Card price columns, one for each card pricer used in search</li>
 * </ol>
 */
@SuppressWarnings("serial")
public class MtgPricerTableModel extends AbstractTableModel {

	/**
	 * The type of a column in {@link MtgPricerTableModel} / {@link CardGrid}
	 */
	public enum MtgPricerColumn {
		NAME("Name"), QUANTITY("Quantity"), RESULT("CardFinder");
	
		private final String headerText;
		
		MtgPricerColumn(String name){
			this.headerText = name;
		}
		
		public String getName(){
			return headerText;
		}
	}
	
	private PricingSettings pricingSettings;
	private Phase currentPhase;
	private Controller controller;
	private List<CardFinder> cardFinders;
	private Map<CardFinder,CardSearchResultSet> resultsContainer;

	/**
	 * Constructs an mtg pricer table model and binds it to a given controller.
	 * @param controller the main controller
	 */
	public MtgPricerTableModel(Controller controller){
		this.controller = controller;
	}

	/**
	 * Sets this table model for a new pricing with a given
	 * pricing settings.
	 * @param settings the settings of the new pricing
	 */
	public void newPricing(PricingSettings settings){
		pricingSettings = settings;
		currentPhase = Phase.SETTING;
	}

	/**
	 * Invoked when the card price search has started 
	 * @param searchResults a collection of the involved card pricers' search results
	 */
	public void searchStarted(Collection<CardSearchResultSet> searchResults){
		resultsContainer = new HashMap<>();
		
		cardFinders = new ArrayList<>();
		for (CardSearchResultSet res : searchResults){ //Store it in the internal hash map for faster access
			resultsContainer.put(res.getFinder(),res);
			cardFinders.add(res.getFinder());
		}
		currentPhase = Phase.SEARCHING;
		fireTableStructureChanged();
	}
	
	@Override
	public Object getValueAt(int row,int column){
		
		Card card  = getCardAt(row);
		
		if (column == MtgPricerColumn.NAME.ordinal())
			return new Cell(card.getName(), Cell.Type.STRING); 
		else if (column == MtgPricerColumn.QUANTITY.ordinal())
			return new Cell(pricingSettings.getQuantity(card) + "", Cell.Type.INTEGER);
		else
			if (currentPhase != Phase.SEARCHING && currentPhase != Phase.SEARCHING)
				throw new AssertionError();
			else{
				CardFinder cardFinder = cardFinders.get(column - 2);
				CardResult result = resultsContainer.get(cardFinder).getCardResult(card);
				
				String val;
				if (result == null) //This card is not processed yet so show just empty string.
					return Cell.NOT_PROCESSED_CELL;
				else if (result == CardResult.NULL_CARD_RESULT) //The card was not found.
					return Cell.NOT_FOUND_CELL;
				else{
					val = result.getPrice() + " " + result.getCurrency().getCurrencyCode();
					return new Cell(val,Cell.Type.PRICE);
				}
			}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(int index){

		return Cell.class;
	}

	@Override
	public int getRowCount(){
		if (currentPhase == null) //When gui objects are constructed but the first phase has not started 
			return 0;
		return pricingSettings.getCards().size();
	}

	@Override
	public int getColumnCount(){
		if (currentPhase == null) //When gui objects are constructed but the first phase has not started 
			return 0;
		
		if (currentPhase == Phase.SETTING)
			return 2;
		else if (currentPhase == Phase.SEARCHING ||
				currentPhase == Phase.PRICING_FINISHED){
			return 2 + cardFinders.size();
		}
		else;
			throw new AssertionError();
	}
	

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex){
		return (currentPhase == Phase.SETTING); //Can edit either card name or quantity when in settings phase
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		MtgPricerColumn column = getColumnType(columnIndex);
		
		if (column == MtgPricerColumn.RESULT){
			CardFinder cardFinder = cardFinders.get(columnIndex - 2);
			return cardFinder.getName();
		}else
			return column.getName();
		
	}
	
	/**
	 * Returns the column type of a column at the specified index.
	 * @param columnIndex the column index
	 * @return the type of the column
	 */
	public MtgPricerColumn getColumnType(int columnIndex){
		if (columnIndex < 2)
			return MtgPricerColumn.values()[columnIndex];
		else
			return MtgPricerColumn.RESULT;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex){

		
		Card card = getCardAt(rowIndex);
		
		if (columnIndex == MtgPricerColumn.NAME.ordinal()){
			String str = (String) value;
			Card newCard = new Card(str.trim());
			
			if (pricingSettings.getCards().contains(newCard)){
				controller.displayErroMessage("<html>The name could not be changed."
						+ " Card <b><i>"  +  newCard.getName()  + "</i></b> is already in the deck </html>");
				return;
			}
			
			pricingSettings.replaceCard(card, newCard);
		}
		else if (columnIndex == 1)
			pricingSettings.setNewQuantity(card, (int) value);
		else
			throw new AssertionError();
	}

	/**
	 * Returns a card for a given row index.
	 * @param row the row index
	 * @return the card at the specified row index 
	 */
	public Card getCardAt(int row){
		return pricingSettings.getCards().get(row);
	}
	
}
