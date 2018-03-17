package haas.olivier.comptes.ctrl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;

/**
 * La situation critique d'un compte.
 * <p>
 * Cette classe contient les informations nécessaires pour connaître les
 * informations importantes d'un compte bancaire au-delà d'une certaine date :
 * solde minimum prévu, date de ce solde minimum, date à laquelle le solde
 * devient débiteur (le cas échéant).
 * <p>
 * La période examinée part de la date spécifiée et court jusqu'à la fin du mois
 * suivant.
 *
 * @author Olivier Haas
 */
public class SituationCritique {

	/**
	 * Le solde le plus faible sur la période.
	 */
	private BigDecimal soldeMini;
	
	/**
	 * La date à laquelle le solde le plus faible est atteint.<br>
	 * Si le solde à la date de départ est le plus faible de la période, alors
	 * il s'agit de la date de départ (même si ce solde était inchangé avant
	 * cette date).
	 */
	private Date dateCreditMini;
	
	/**
	 * La date à laquelle le compte est débiteur pour la première fois sur la
	 * période, ou <code>null</code> s'il est toujours créditeur.
	 */
	private Date dateDebit;
	
	/**
	 * Construit une situation critique du compte.
	 * 
	 * @param compte	Le compte à examiner.
	 * @param today		La date à partir de laquelle examiner la situation du
	 * 					compte.
	 * 
	 * @throws IOException
	 */
	public SituationCritique(Compte compte, Date today) throws IOException {
		Month month = new Month(today);
		Date start = getMidnight(today);
		
		// Étudier le mois actuel et le mois suivant
		explore(compte.getHistoriqueIn(month).iteratorFrom(start));
		explore(compte.getHistoriqueIn(month.getNext()).iterator());
	}
	
	/**
	 * Renvoie une date égale au jour de la date spécifiée sans tenir compte des
	 * heures, minutes, secondes et millisecondes.
	 * 
	 * @param date	Une date.
	 * 
	 * @return		Une date égale au jour de <code>date</code> à minuit.
	 */
	private static Date getMidnight(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * Parcourt les soldes journaliers et détermine les dates et soldes
	 * critiques.
	 * 
	 * @param soldesIterator	Un itérateur de soldes journaliers.
	 */
	private void explore(Iterator<Entry<Date, BigDecimal>> soldesIterator) {
		while (soldesIterator.hasNext()) {
			Entry<Date, BigDecimal> soldeAtDate = soldesIterator.next();
			BigDecimal solde = soldeAtDate.getValue();
			if (soldeMini == null || solde.compareTo(soldeMini) < 0) {
				soldeMini = solde;
				dateCreditMini = soldeAtDate.getKey();
			}
			if (dateDebit == null && solde.signum() < 0) {
				dateDebit = soldeAtDate.getKey();
			}
		}
	}
	
	
}
