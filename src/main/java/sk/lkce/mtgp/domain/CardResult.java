package sk.lkce.mtgp.domain;

import java.util.Currency;

import sk.lkce.mtgp.cardsearch.CardFinder;

/**
 * A representation of a result of search for a particular {@link Card} from one {@link CardFinder}. Contains
 * all the information about the card which were available from the search results. Even though
 * it might seems strange that the name of the card is included, the reason behind this is the fact
 * that the search engines of some vendors return also similarly named cards and therefore a search
 * result for a particular card might not be related at all to that particular card.
 */
public class CardResult{
	
	private String name;
	private String type;
	private String edition;
	private double price;
	private String notFoundMsg;
	private Currency currency;

	/** Null object for no result*/
	public static final CardResult NULL_CARD_RESULT = createNullCardResult();
	
	private CardResult(){}

	/**
	 * Constructs a card result with given attributes of the card 
	 * which was present in the search result.
	 * @param name name of the card
	 * @param type type of the card (common, uncommon etc.), is only sometimes included
	 * @param edition the edition of the card
	 * @param price the numeric part of the price of the card 
	 * @param currency the currency part of the price
	 */
	public CardResult(String name, String type, String edition,
						double price,Currency currency){
		if (name != null)
			this.name = name.replaceAll("[`�]", "'");
		this.type = type;
		this.edition = edition;
		this.price = price;
		this.currency = currency;
	}

	/**
	 * Creates a null card result object.
	 */
	private static CardResult createNullCardResult(){
		CardResult cr =  new CardResult();
		
		String na = "N/A";
		
		cr.name = na;
		cr.price = -1;
		cr.edition = na;
		cr.type = na;
		return cr;
	}


	/**
	 * Returns the name of the card in this card search result.
	 * @return the name of the card
	 */
	public String getName() {
		return name;
	}


	/**
	 * Returns the type of the card in this card search result.
	 * This attribute is optional and not all card results might have it.
	 * @return the type of the card
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the edition of the card in this card search result.
	 * This attribute is optional and not all card results might have it.
	 * @return
	 */
	public String getEdition() {
		return edition;
	}

	/**
	 * Returns the numeric part of card's price in this card search result.
	 * @return the price of the card as numeric value
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * Returns the currency part of card's price in this card search result.
	 * @return the currency of the card's price
	 */
	public Currency getCurrency(){
		return currency;
	}
	
	
	@Override
	public String toString(){
		
		if (notFoundMsg != null)
			return "N/A: " + notFoundMsg;
		
		return this.getClass().getSimpleName() + "[ " + name + ", " + type +", " + 
				", " + edition  +", " + price + "]";
	}

}
