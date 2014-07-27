package sk.lkce.mtgp.cardsearch;

import java.util.ArrayList;
import java.util.List;


/**
 * A card finder factory.
 * @see CardFinder
 */
public class CardFinderFactory {

	private static CernyRytirCardFinder cernyRytir;
	private static ModraVeverickaCardFinder modraVevericka;
	private static DragonHostCardFinder dragon;
	
	/**
	 * Returns a list with all the card finders which are provided by this factory
	 * @return
	 */
	public static List<CardFinder> allCardFinders(){

		List<CardFinder> finders = new ArrayList<>();
		finders.add(getCernyRytirPricer());
		finders.add(getDragonPricer());
		finders.add(getModraVeverickaPricer());
		return finders;
	}
	
	/**
	 * Returns and instance of {@link CernyRytirCardFinder}
	 * @return an instance of {@link CernyRytirCardFinder}
	 */
	public static CardFinder getCernyRytirPricer(){
		if (cernyRytir == null)
			cernyRytir = new CernyRytirCardFinder();
		return cernyRytir;
	}
	
	/**
	 * Returns an instance of {@link ModraVeverickaCardFinder}
	 * @return an instance of {@link ModraVeverickaCardFinder}
	 */
	public static CardFinder getModraVeverickaPricer(){
		if (modraVevericka == null)
			modraVevericka = new ModraVeverickaCardFinder();
		return modraVevericka;
	}
	
	/**
	 * Returns an instance of {@link DragonCardFinder}
	 * @return an instance of {@link DragonCardFinder}
	 */
	public static CardFinder getDragonPricer(){
		if (dragon == null)
			dragon = new DragonHostCardFinder();
		return dragon;
	}

}
