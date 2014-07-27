package sk.lkce.mtgp;

import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

import sk.lkce.mtgp.gui.Controller;

/**
 * An entry class for the application which creates
 * all the fundamental components.
 */
public class MtgPricerMain {

	public MtgPricerMain() {
		setLookAndFeel();
		Controller controller = new Controller();
		controller.newPricing();
	}
	
	
	public static void main(String[] args){
		
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				new MtgPricerMain();
			}
		});
	}

	/**
	 * Set the platform look and feel. TODO FINISH and REVIEW!!!!
	 */
	private void setLookAndFeel(){
		try {
			
			String lookAndFeel= javax.swing.UIManager.getSystemLookAndFeelClassName(); 
			
			System.out.println(lookAndFeel);
			if (lookAndFeel.endsWith("MetalLookAndFeel")) //This might be Linux so let's try GTK look and feel.
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			
			javax.swing.UIManager.setLookAndFeel(lookAndFeel); 
			
			javax.swing.UIManager.getDefaults().put("Button.showMnemonics", Boolean.TRUE);
		} catch (ClassNotFoundException e) {
			//Nothing to do here. Just print to err stream.
			e.printStackTrace();
		} catch (InstantiationException e) {
			//Nothing to do here. Just print to err stream.
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			//Nothing to do here. Just print to err stream.
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			//Nothing to do here. Just print to err stream.
			e.printStackTrace();
		}
	}
	
}
