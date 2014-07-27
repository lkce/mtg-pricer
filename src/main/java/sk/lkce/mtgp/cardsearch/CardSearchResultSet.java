package sk.lkce.mtgp.cardsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.lkce.mtgp.domain.Card;
import sk.lkce.mtgp.domain.CardResult;

/**
 * A container of search results for a given card finder. Has read-only public interface.
 * Updating and reading from card result set is synchronised and 
 * intermediate results can be read from it while new results are being added.
 */
public class CardSearchResultSet {
	
	private long searchTime;
	private final List<Card> notFound = new ArrayList<>();
	private final List<Card> notFoundRO = Collections.unmodifiableList(notFound);
	private final CardFinder finder;
	private final Map<Card, CardResult> results;
	private final Map<Card, CardResult> resultsView;

	/**
	 * Creates a new card search result set for a given card finder.
	 * @param finder
	 */
	public CardSearchResultSet(CardFinder finder){
		this.finder = finder;  
		results = new HashMap<>();
		resultsView = Collections.unmodifiableMap(results);
	}

	/**
	 * Returns a card result for a given card. If there is no
	 * corresponding card <code>null</code> is returned. If the
	 * search for a given card was attempted but with no result,
	 * {@link CardResult#NULL_CARD_RESULT} is returned.
	 * 
	 * @param card the card for which results should be retrieved
	 * @return the card result or <code>null</code>
	 */
	public synchronized CardResult getCardResult(Card card){
		return results.get(card);
	}
	
	/**
	 * Returns unmodifiable  card - card result map.
	 * @return card - result map
	 */
	public synchronized Map<Card,CardResult> getCardResults(){
		return resultsView;
	}

	/**
	 * Returns a card finder associated with this card search result set.
	 * @return the card finder of this result set
	 */
	public CardFinder getFinder(){
		return finder;
	}

	
	/**
	 * Returns the cards which were not found in the search.
	 * A card is considered not found if an attempt to find it returned no result.
	 * @return list of cards which were not found
	 */
	public synchronized List<Card> getNotFoundCards(){
		return notFoundRO;
	}
	
	/**
	 * Returns the time of the whole search took in milliseconds.
	 * @return the time of the search in milliseconds
	 */
	public synchronized long getSearchTime(){
		return searchTime;
	}
	
	/**
	 * Adds card result for a given card to this card search result set.
	 * @param card the card
	 * @param result the card result
	 */
	synchronized void addCardResult(Card card, CardResult result){
		results.put(card, result);
	}
	
	/**
	 * Adds a card to the list of cards which were not found.
	 * @param card the card which was not found
	 */
	synchronized void addNotFound(Card card) {
		notFound.add(card);
		results.put(card, CardResult.NULL_CARD_RESULT);
	}
	
	/**
	 * Sets the time of the whole search  in milliseconds.
	 * @param time time of the search
	 */
	synchronized void setSearchTime(long time) {
		searchTime = time;
	}
	
	
	
}
