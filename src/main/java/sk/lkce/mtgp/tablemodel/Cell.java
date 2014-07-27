package sk.lkce.mtgp.tablemodel;


/**
 * A representation of the cell in the table model - {@link MtgPricerTableModel}.
 * The cell contains a text and is of a given cell type.
 */
public class Cell implements Comparable<Cell>{
	
	/**
	 * Type of cell. Useful for comparing and ordering of the cells.
	 */
	public static enum Type {STRING, PRICE, INTEGER};
	
	public static Cell NOT_FOUND_CELL = new Cell("not found", Type.PRICE);
	public static Cell NOT_PROCESSED_CELL = new Cell("", Type.PRICE);
	
	private String text;
	private Type type;
	
	public Cell(String text, Type type){
		this.text = text;
		this.type = type;
	}
	
	/**
	 * Returns this cells's text.
	 * @return the text inside this cell
	 */
	public String getText(){
		return text;
	}
	
	/**
	 * Returns this cells's type.
	 * @return the type of this cell
	 */
	public Type getType(){
		return type;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + " [ text:"  + text  + ", type: " + type + "]";
	}

	@Override
	public int compareTo(Cell anotherCell) {
	
		//Node - "NA" = not available/not found cell.
		
		//If the first is not loaded
		if (this == Cell.NOT_PROCESSED_CELL)
			//..and the second is not.
			if (anotherCell != Cell.NOT_PROCESSED_CELL)
				return -1;
			//if both are NA.
			else
				return 0;
		//If only second is NA.
		if (anotherCell == Cell.NOT_PROCESSED_CELL)
			return 1;
		
		//If the first is NA...
		if (this == Cell.NOT_FOUND_CELL)
			//..and the second is not.
			if (anotherCell != Cell.NOT_FOUND_CELL)
				return -1;
			//if both are NA.
			else
				return 0;
		//If only second is NA.
		if (anotherCell == Cell.NOT_FOUND_CELL)
			return 1;
		
		
		if (this.getType() != anotherCell.getType())
			throw new IllegalArgumentException("Cells are not of the same type!");
		
		if (this.getType() == Cell.Type.STRING)
			return this.getText().compareTo(anotherCell.getText());
		
		if (this.getType() == Cell.Type.PRICE){
			
			//Split the format '##.## CURRENCY' and take the first token -  double value part (get rid of currency characters).
			Double d1 = Double.parseDouble(this.getText().split(" ")[0]); 
			Double d2 = Double.parseDouble(this.getText().split(" ")[0]);
			return d1.compareTo(d2); 
		}
		if (this.getType() == Cell.Type.INTEGER){
			Integer i1 = Integer.parseInt(this.getText());
			Integer i2 = Integer.parseInt(anotherCell.getText());
			return i1.compareTo(i2); 
		}
		
		throw new AssertionError();
	}
}
