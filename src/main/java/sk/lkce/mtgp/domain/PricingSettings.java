package sk.lkce.mtgp.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.lkce.mtgp.cardsearch.CardFinder;


/**
 * A collection of all settings necessary to start a pricing process (card price search).
 * It mainly contains list of cards to be searched for and the list of card finders to be used.
 * @see Card
 * @see CardFinder
 */
public class PricingSettings {
	
	private final Map<Card,Integer>  cardQuantityMap = new HashMap<>();
	private final List<Card> cardList = new ArrayList<>(); //For keeping track of insertion orde
	private final List<Card> roCardList = Collections.unmodifiableList(cardList);
	private final List<CardFinder> finders = new ArrayList<>();
	
	
	/**
	 * Adds a card to the list of cards to be priced along with 
	 * its quantity.
	 * @param card the card to be added
	 * @param quantity the card's quantity
	 * @throws IllegalArgumentException if <code>newQuantity</code> is lower than 1
	 * or if the card is already part of these settings
	 */
	public void addCard(Card card, int quantity){
		checkIfCardIsInCollection(card, false);
		if (quantity < 1)
			throw new IllegalArgumentException("The quantity needs to be at least 1");
			
		cardQuantityMap.put(card, quantity);
		cardList.add(card);
	}
	
	/**
	 * Removes a card from the list of card to be priced.
	 * @param card the card to be removed
	 * @throws IllegaArgumentException if the card is not in the list
	 */
	public void removeCard(Card card){
		checkIfCardIsInCollection(card, true);
		
		cardList.remove(card);
		cardQuantityMap.remove(card);
	}

	/**
	 * Replaces a card in the list of the cards to be priced with
	 * another card while preserving the card quantity. This is convenience method and
	 * is basically equal to removing the old card and adding a new one with the same quantity
	 * as the old one has.
	 * @param oldCard the card to be replaced
	 * @param newCard the new card
	 * @throws IllegaArgumentException if either the old card is not in the list or
	 * the new card is in the list
	 */
	public void replaceCard(Card oldCard, Card newCard){
		checkIfCardIsInCollection(oldCard, true);
		checkIfCardIsInCollection(newCard, false);
		int index = cardList.indexOf(oldCard);
		int quantity = cardQuantityMap.get(oldCard);
		
		removeCard(oldCard);
		cardQuantityMap.put(newCard, quantity);
		cardList.add(index, newCard);
		
	}
	
	
	/**
	 * Returns unmodifiable list of cards which should be priced.
	 * @return the list of cards to be priced
	 */
	public List<Card> getCards(){
		return roCardList;
	}

	/**
	 * Returns a quantity for a given card. 
	 * @param card the card
	 * @return card's quantity
	 */
	public int getQuantity(Card card){
		return cardQuantityMap.get(card);
	}

	/**
	 * Sets the new quantity for a given card.
	 * @param card the card which quantity should be changed
	 * @param newQuantity the new value of card's quantity
	 * @throws IllegalArgumentException if <code>newQuantity</code> is lower than 1
	 */
	public void setNewQuantity(Card card, int newQuantity){
		checkIfCardIsInCollection(card, true);
		if (newQuantity < 1)
			throw new IllegalArgumentException("The quantity needs to be at least 1");
		cardQuantityMap.put(card, newQuantity);
	}

	/**
	 * Adds a card finder to the list of the card finders to be used during card pricing.
	 * @param finder a new card finder to be added
	 * @throws IllegalArgumentException if the card-finder has been already added
	 */
	public void addFinder(CardFinder finder){
		if (finders.contains(finder))
			throw new IllegalArgumentException("The finder has been already added");
		finders.add(finder);
	}
	
	/**
	 * Removes a card finder from the list of the card finders to be used during card pricing.
	 * @param finder a new card finder to be removed
	 * @throws IllegalArgumentException if the card-finder is not in the list of card finders
	 */
	public void removeFinder(CardFinder finder){
		if (!finders.contains(finder))
			throw new IllegalArgumentException("No such finder in the settings");
		finders.remove(finder);
	}
	

	/**
	 * Returns an unmodifiable collection of card finders which are set to be used
	 * in the card search.
	 * @return
	 */
	public Collection<CardFinder> getFinders(){
		return Collections.unmodifiableCollection(finders);
	}

	/**
	 * Checks if a card is or is not in the list of cards to be priced
	 * and throws exception
	 * if the condition is not met.
	 */
	private void checkIfCardIsInCollection(Card card, boolean shouldBePresent){
		if (card == null)
			throw new NullPointerException();
		
		boolean isPresent = cardQuantityMap.get(card) != null;
		
		if (shouldBePresent && !isPresent)
			throw new IllegalArgumentException("No such card ( " + card + ") in collection");
		else if (!shouldBePresent && isPresent)
			throw new IllegalArgumentException("The card " + card + ") is already in collection");
	}
	
}
