package haas.olivier.comptes.ctrl;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import haas.olivier.util.Month;

/** Un itérateur des jours d'un mois dans l'ordre inverse de l'ordre
 * chronologique.
 * 
 * @author Olivier Haas
 */
class ReverseDaysIterator implements Iterator<Date> {
	
	/** Le calendrier. */
	private final Calendar calendar = Calendar.getInstance();
	
	/** Le premier jour du mois. C'est la dernière date à renvoyer. */
	private final Date limit;
	
	/** Construit un itérateur des jours d'un mois dans l'ordre inverse de
	 * l'ordre chronologique.
	 * 
	 * @param month	Le mois à parcourir.
	 */
	public ReverseDaysIterator(Month month) {
		calendar.setTime(month.getNext().getFirstDay());
		limit = month.getFirstDay();
	}// constructeur

	@Override
	public boolean hasNext() {
		return limit.before(calendar.getTime());
	}

	@Override
	public Date next() {
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar.getTime();
	}
	

}
