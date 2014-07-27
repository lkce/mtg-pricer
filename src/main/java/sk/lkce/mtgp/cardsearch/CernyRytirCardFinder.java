package sk.lkce.mtgp.cardsearch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import sk.lkce.mtgp.domain.CardResult;

/**
 * Implementation of {@link CardFinder} for web page <b>http://www.cernyrytir.cz/</b>.
 *
 */
class CernyRytirCardFinder extends CardFinder{
	
	private static final int RESULT_PER_PAGE = 30;
	private static final String URL = "http://www.cernyrytir.cz/";
	private static final String NAME = "Cerny Rytir";
	private static Currency CURRENCY = Currency.getInstance("CZK");

	/**
	 * Constructor with the default access modifier.
	 */
	CernyRytirCardFinder(){}


	@Override
	public List<CardResult> getCardResults(String cardName) throws IOException{
		
		/** 1. load the cards from the first page (might be last as well)*/
		
		//Get the html result page from the query as String.
		String html =  getHTMLString(createURL(cardName,1));
		
		List<CardResult> foundCards = new ArrayList<CardResult>();
		
		//Add all results we found on the first page.
		foundCards.addAll(extractCardsFromHtml(html));
		
		
		/**2. check for additional pages and load results from them as well*/
		
		//Determine if there are also additional pages.
		Document doc = Jsoup.parse(html);
		Elements span = doc.select("span.kusovkytext");
		
		//If the special element exists, it has more pages -> calculate how many.
		if (span.size() > 0){
			int resultsCount  = (int) getDoubleFromString(span.text(),1);
			int pagesTotal = (int) Math.ceil((float) resultsCount / RESULT_PER_PAGE);
			
			//Load cards from other pages as well.
			for (int i =2; i <= pagesTotal; i++){
				html = getHTMLString(createURL(cardName,i));
				foundCards.addAll(extractCardsFromHtml(html));
			}
		}
		return foundCards;
	}
	
	
	/**
	 * Parses a given html document and returns list of found cards results.
	 * @param html the html document in string form
	 * @return list parsed card results
	 */
	public List<CardResult> extractCardsFromHtml(String html){
		
		Document doc = Jsoup.parse(html);
		
		List<CardResult> foundCards = new ArrayList<CardResult>();
		
		//Find second table with kusovkytext class which contains the elements with info.
		//Extract table rows containing the required info.
		Elements resultRows = doc.select("table.kusovkytext").get(1).select("tbody > tr");
		
		String name = null; 
		String edition = null;
		String type = null;
		String price = null;
		
		for (int i = 0; i < resultRows.size(); i++){
			
			int modRes = i % 3;
			
			//1st row
			if (modRes == 0)
				name = resultRows.get(i).select("td div font").text();
			//2nd row
			else if (modRes == 1)
				edition = resultRows.get(i).select("td:eq(0)").text();
			//Last row -> modRes == 2
			else{
				type = resultRows.get(i).select("td:eq(0)").text();
				price = resultRows.get(i).select("td:eq(2)").text();
				//Add card
				foundCards.add(new CardResult(name,type, edition, 
						getDoubleFromString(price,1), CURRENCY));
			}
		}
		
		return foundCards;
	}
	

	/**
	 * Creates URL which navigates to a given page of results set
	 * for a given card. The page size is {@link #RESULT_PER_PAGE}.
	 * @param cardName the name of the card to be found
	 * @param page the number of the page of card results set
	 * @return the url which navigates to the specified results page
	 */
	private String createURL(String cardName, int page){
		
		final String addressCR = URL + "index.php3";
		final String urlParam = "akce=3&"
				
				+ "limit="+ (page-1) * RESULT_PER_PAGE
				+ "&jmenokarty="+ cardName.replace(" ", "+")
				
				+ "&edice_magic=libovolna&poczob=" + RESULT_PER_PAGE
				+ "&foil=A&"
				+ "triditpodle=ceny&hledej_pouze_magic=1&submit=Vyhledej";
		
		return addressCR + "?" + urlParam;
	}

	@Override
	public String getURL() {
		return URL;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Currency getCurrency() {
		return CURRENCY;
	}

	@Override
	public java.net.URL getURLForCard(String cardName){
		
		String normalizedName = normalizeCardName(cardName);
		java.net.URL url;
		try {
			url = new java.net.URL(createURL(normalizedName,1));
		} catch (MalformedURLException e) {
			//This should not happen. Re-throw it anyway.
			throw new RuntimeException(e);
		}
		return url;
	}
	
	

	
}
