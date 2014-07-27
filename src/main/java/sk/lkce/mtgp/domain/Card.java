package sk.lkce.mtgp.domain;

/**
 * A representation of a Magic The Gathering card.
 * It can be used as value object.
 */
public class Card{
	
	private String name;

	/**
	 * Constructs a card with a given name.
	 * @param name the name of the card
	 */
	public Card(String name){
		this.name = name;
	}

	/**
	 * Returns the name of this card.
	 * @return the card's name
	 */
	public String getName(){
		return name;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof Card))
			return false;
		Card c = (Card) o;
		return c.name.equals(name);
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName() + "[ name: " + name +  "]";
	}
}
