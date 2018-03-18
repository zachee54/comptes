package haas.olivier.comptes.ctrl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;

/**
 * Les soldes quotidiens d'un compte pendant un mois donné.<br>
 * Il peut s'agir de soldes théoriques ou réels, selon la source utilisée.
 *
 * @author Olivier Haas
 */
public class DailySolde implements Iterable<Entry<Date, BigDecimal>> {
	
	/**
	 * Les soldes quotidiens.
	 */
	private final SortedMap<Date,BigDecimal> soldesByDay = new TreeMap<>();

	/**
	 * Construit les soldes quotidiens d'un compte sur un mois.
	 * 
	 * @param compte	Le compte dont on souhaite les soldes.
	 * @param month		Le mois au cours duquel on souhaite les soldes.
	 * @param pointages	<code>true</code> si on veut les soldes réels basés sur
	 * 					les pointages, <code>false</code> si on souhaite les
	 * 					soldes théoriques basés sur les dates d'écritures.
	 * 
	 * @throws IOException
	 */
	public DailySolde(Compte compte, Month month, boolean pointages)
			throws IOException {

		// Solde de fin de mois (on commence par là)
		BigDecimal solde = pointages
				? compte.getSoldeAVue(month)
				: compte.getHistorique(month);
		
		// Les écritures pour chaque jour
		EcrituresByDay ecrituresByDay = new EcrituresByDay(month, pointages);

		// Parcourir tous les jours du mois, à l'envers
		Iterator<Date> reverseDays = new ReverseDaysIterator(month);
		do {
			// Solde en fin de journée
			Date date = reverseDays.next();
			soldesByDay.put(date, solde);

			// Traiter les écritures de cette journée
			for (Ecriture e : ecrituresByDay.getEcritures(date))
				solde = solde.subtract(compte.getImpactOf(e));
		} while (reverseDays.hasNext());
	}
	
	@Override
	public Iterator<Entry<Date, BigDecimal>> iterator() {
		return soldesByDay.entrySet().iterator();
	}
	
	/**
	 * Renvoie une itération des soldes à partir de la date spécifiée.
	 * 
	 * @param date	La date à laquelle commencer l'itération.
	 * 
	 * @return		Un itérateur dont la première date est <code>date</code>, ou
	 * 				le début du mois si <code>date</code> est antérieure, ou un
	 * 				itérateur vide si <code>date</code> est postérieure au mois.
	 */
	public Iterator<Entry<Date, BigDecimal>> iteratorFrom(Date date) {
		return soldesByDay.tailMap(date).entrySet().iterator();
	}
	
	/**
	 * Renvoie le solde à une date précise.
	 * 
	 * @param date	La date souhaitée.
	 * 
	 * @return		Le solde à la date souhaitée, ou <code>null</code> si la
	 * 				date n'est pas incluse dans le mois traité par cette
	 * 				instance.
	 */
	public BigDecimal getSoldeAt(Date date) {
		return soldesByDay.get(date);
	}
}
