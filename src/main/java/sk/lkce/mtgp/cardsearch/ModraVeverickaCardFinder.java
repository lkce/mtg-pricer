package sk.lkce.mtgp.cardsearch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sk.lkce.mtgp.domain.CardResult;

/**
 * Implementation of {@link CardFinder} for web page <b>http://www.modravevericka.sk/</b>.
 *
 */
class ModraVeverickaCardFinder extends CardFinder{

	//public static final int RESULT_PER_PAGE = 50;
	private static final String URL = "http://www.modravevericka.sk/";
	private static final String NAME = "Modra Vevericka";
	private static Currency CURRENCY = Currency.getInstance("EUR");

	/**
	 * Constructor with the default access modifier.
	 */
	ModraVeverickaCardFinder() {}
	

	@Override
	List<CardResult> getCardResults(String normalizedCardName) throws IOException {
		String html = getHTMLString(createSearchUrl(normalizedCardName));
		return extractCardsFromHtml(html);
	}

	/**
	 * Parses a given html document and returns list of found cards results.
	 * @param html the html document in string form
	 * @return list parsed card results
	 */	
	private List<CardResult> extractCardsFromHtml(String html){
		
		Document doc = Jsoup.parse(html);
		List<CardResult> foundCards = new ArrayList<CardResult>();

		Elements resultRows = doc.select("#card_list").select("div.card");
		
		String name = null; 
		String edition = "N/Atyrrtyr";
		String type = "N/A";
		String price = null;
		
		for (int i = 0; i < resultRows.size(); i++){
			
			Element card = resultRows.get(i);
			name = card.select("div.name a").text();
			price = card.select("div.price").text();
			foundCards.add(new CardResult(name,type, edition, 
										getDoubleFromString(price,1), CURRENCY));
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
	private String createSearchUrl(String cardName){
		//Number of page size specified in URL - 10000.
		final String  queryString="x-cards,x-page-1-size-10000-order-name-asc.html?onclick=run_shopping_assistant&"
				+ "filter_name=" + cardName.replace(" ", "+");
		return URL + queryString;
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
	public java.net.URL getURLForCard(String cardName) {
		String normalizedName = normalizeCardName(cardName);
		java.net.URL url;
		try {
			url = new java.net.URL(createSearchUrl(normalizedName));
		} catch (MalformedURLException e) {
			//This should not happen. Re-throw it anyway.
			throw new RuntimeException(e);
		}
		return url;
	}
	

}
