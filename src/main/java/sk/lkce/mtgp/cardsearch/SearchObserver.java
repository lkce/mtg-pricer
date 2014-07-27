package sk.lkce.mtgp.cardsearch;

import sk.lkce.mtgp.domain.Card;
import sk.lkce.mtgp.domain.CardResult;

/**
 * An observer for events related to card search. <p>
 * 
 *@see {@link SearchExecutor}
 *@see {@link CardFinder}
 */
public interface SearchObserver {
	
	/**
	 * Invoked when the card search has started. 
	 * @param numberOfCards the number of cards in the search
	 */
	void searchStarted(int numberOfCards);
	
	/**
	 * Invoked when a card finder thread started searching for a card.
	 * @param card the card for which the finder has started searching
	 * @param finder the card finder involved
	 */
	void cardSearchStarted(Card card, CardFinder finder);
	
	/**
	 * Invoked when a finder thread has finished searching for a card.
	 * @param card the card for which the finder finished searching
	 * @param result the result of the card search
	 * @param finder the card finder involved
	 */
	void cardSearchFinished(Card card, CardResult result, CardFinder finder);
	
	/**
	 * Invoked when a card finder search thread successfully ended.
	 * @param finder the card finder involved
	 */
	void searchThreadFinished(CardFinder finder, CardSearchResultSet results);
	
	/**
	 * Invoked when a card finder search thread encountered an error and has been forced to stop.
	 * @param finder the card finder involved
	 * @param t the cause
	 */
	void searchThreadFailed(CardFinder finder, Throwable t);

	/**
	 * Invoked when the whole search has completed.
	 * @param interrupted <code>true</code> if the search was interrupted by the user
	 */
	void searchingFinished(boolean interrupted);
	
}
