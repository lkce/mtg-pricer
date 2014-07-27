package sk.lkce.mtgp.cardsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.lkce.mtgp.domain.CardResult;

/**
 * An abstract class which provides base for implementation of specific ways how to scrap
 * the card prices from a html page.
 * 
 * TODO: reduce the duplicate code across implementing classes. Evidently more
 * functionality can be made abstract.
 *
 */
public abstract class CardFinder {
	
	/**
	 * Retrieves the list of cards that match the card name.
	 * @param cardName Name of the card to be found.
	 * @return List of found cards.
	 * @throws IOException
	 */
	abstract List<CardResult> getCardResults(String normalizedCardName) throws IOException;

	/**
	 * Returns URL of the web page from which this card finder scraps
	 * card prices data
	 * @return the url of the associated web page
	 */
	public abstract String getURL();
	
	/**
	 * Returns the name of this card pricer
	 * @return card pricer's name
	 */
	public abstract String getName();
	
	/**
	 * Returns currency of the card prices for this card finder
	 * @return card prices currency of this card finder
	 */
	public abstract Currency getCurrency();
	
	/**
	 * Returns built URL which display search results of a given
	 * card on the web page associated with this card finder
	 * @param cardName
	 * @return
	 */
	public abstract URL getURLForCard(String cardName);
	
	
	/**
	 * Finds the card result for a given card which has the
	 * lowest price
	 * @param cardName the name of the mtg card
	 * @return card results for the card with the lowest price
	 * @throws IOException
	 */
	public CardResult findCheapestCard(String cardName) throws IOException{
		
		String normalizedCardName = normalizeCardName(cardName);
		List<CardResult> foundCards = getCardResults(normalizedCardName);
		
		/* Remove cards which does not exactly match the name
		 * e.g. Mountain search return Goblin Mountaineer as well.
		 * Also foil version of cards (Mountain - foil) will be removed (they are more expensive anyway).
		 */
		
		normalizedCardName = normalizedCardName.replaceAll("[`�]", "'"); //Be sure to have "'" instead of "`"  and "�"
		//so it can be compared.
		
		
		if (foundCards == null || foundCards.size() < 1)
			return null;
		
		
		for (CardResult card: new ArrayList<CardResult>(foundCards))
			if (!card.getName().equalsIgnoreCase(normalizedCardName))
				foundCards.remove(card);
		
		//If all the results have been filtered out.
		if (foundCards.size() < 1)
			return null;
		
		//Select the cheapest card.
		CardResult cheapest = foundCards.get(0);
		
		for (CardResult c : foundCards)
			if (cheapest.getPrice() > c.getPrice())
				cheapest = c;
		
		return cheapest;
	} 
	
	/**
	 * Custom equals implementation. Two CardFinders are equals when 
	 * they have same <code>NAME</code> and same <code>URL</code>. 
	 */
	@Override
	public boolean equals(Object o){
		if (o == this)
			return true;
		if (o.getClass() != getClass()) 
			return false;
		CardFinder cf = (CardFinder) o;
		
		return getURL().equals(cf.getURL()) && 
				getName().equals(cf.getName());
	}
	
	@Override
	public int hashCode(){
		return (getName() + getURL()).hashCode();
	}
	
	/**
	 * Retrieves HTML document as string for a given URL.
	 * @param address URL of the web page
	 * @return html document as string
	 * @throws IOException
	 */
	static String getHTMLString(String address) throws IOException{
		URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
        
        connection.setRequestMethod("GET");

        //Get Response
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while((line = rd.readLine()) != null) {
          response.append(line);
          response.append('\r');
        }
        rd.close();

       return response.toString();
	}
	
	/**
	 * Returns the first occurrence of a number in a string. If not found, an exception is thrown.
	 * @param text String containing the number
	 * @return found number
	 */
	static double getDoubleFromString(String text, int position){
		
		String regex = "[\\d.,]+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		
		for (int i =0; i < position;i++)
			m.find();
		String no = m.group().replace(",",".");
		
		return Double.parseDouble(no);
		
	}
	
	/**
	 * Counts the number of regular expression matches in a string.
	 * @param text <code>String</code> where to look in
	 * @param regex regular expression to look for
	 * @return number of occurrences of regular expression in a string
	 */
	static int countRegexMatches(String text, String regex){
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		int counter =0;
		
		while(m.find())
			counter++;
		
		return counter;
	}
	
	
	/**
	 * Transforms the name of an mtg card containing more spaces than standard
	 * to normalized format. E.g. <i> Flames     of   Firebrand" to "Flames of Firebrand"</i>.
	 * @param cardName
	 * @return normalized card name
	 */
	static String normalizeCardName(String cardName){
		//Trim and split it.
		String[] nameParts = cardName.trim().split("\\s+");
		String newCardName = nameParts[0];
		
		//No need for string builder - card names do not consist of many words.
		for (int i = 1; i < nameParts.length; i++)
			newCardName += " " + nameParts[i];
		
		return newCardName;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name: " + getName() + "]";
	}
	
	
	
}
