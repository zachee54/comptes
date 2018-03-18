package haas.olivier.comptes.gui.actions;

import haas.olivier.util.Month;

import java.io.IOException;
import java.util.Date;

/**
 * Un observateur de changements de mois ou de date.
 */
public interface MonthObserver {

	/**
	 * Notifie un changement de mois.
	 */
	void monthChanged(Month month);

	/**
	 * Notifie un changement de date. Cette méthode ne doit pas être appelée en
	 * plus de monthChanged, bien qu'un changement de mois implique que la date
	 * ait changée aussi.
	 * 
	 * @throws IOException
	 */
	void dateChanged(Date date) throws IOException;
}
