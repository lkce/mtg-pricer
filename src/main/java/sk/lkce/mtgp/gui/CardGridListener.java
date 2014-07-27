package sk.lkce.mtgp.gui;

/**
 * An observer for the {@link CardGrid} specific events.
 */
public interface CardGridListener {
	
	/**
	 * Invoked when a card grid has lost focus.
	 */
	void gridFocusLost();
	
	/**
	 * Invoked when a card grid has gained focus.
	 */
	void gridFocusGained();

	/**
	 * Invoked when the selection in a card grid has changed.
	 * @param selectedRows indexes of the newly selected rows
	 */
	void gridSelectionChanged(int[] selectedRows);
	
}
