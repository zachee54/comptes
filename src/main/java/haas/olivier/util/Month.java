/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** Un mois calendaire (mois et année).
 * 
 * @author Olivier HAAS
 */
public class Month implements Comparable<Month>, Serializable {
	private static final long serialVersionUID = 4137756502743120255L;

	/** Le format d'affichage. */
	private static final DateFormat DF = new SimpleDateFormat("MMMM yyyy");
	
	/** La date sous-jacente, fixée au premier jour du mois. */
	private final Date date;
	
	/** L'année. */
	private final int annee;
	
	/** Le numéro du mois. */
	private final int mois;
	
	/** Construit un mois correspondant au mois en cours. */
	public Month() {
		this(new Date());
	}// constructeur
	
	/** Construit un mois calendaire correspondant à celui de la date
	 * spécifiée.
	 * 
	 * @param date	La date dont on veut extraire le mois, ou <code>null</code>
	 * 				pour obtenir le mois en cours.
	 */
	public Month(Date date) {
		this(date == null						// Si date null
				? new Date().getTime()			// Utiliser la date actuelle
				: date.getTime());				// Cas général : date spécifiée
	}// constructeur Date
	
	/** Construit un mois calendaire correspondant à celui de la date
	 * spécifiée.
	 * 
	 * @param time	Un <code>long</code> correspondant à la date dont on veut
	 * 				extraire le mois, ou <code>null</code> pour obtenir le mois
	 * 				en cours.
	 */
	public Month(long time) {
		
		// Définir un calendrier à la date spécifiée
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		
		// Neutraliser tous les champs pour ne garder que le mois et l'année
		eraseTime(cal);
		
		// Conserver cette date
		this.date = cal.getTime();
		annee = cal.get(Calendar.YEAR);
		mois = cal.get(Calendar.MONTH) + 1;
	}// constructeur long
	
	/** Construit un mois à partir de l'année et du numéro du mois.
	 * 
	 * @param annee	L'année.
	 * @param mois	Le numéro du mois.
	 */
	// TODO à tester
	public Month(int annee, int mois) {
		this.annee = annee;
		this.mois = mois;
		
		// Créer une date pour ce mois et cette année
		Calendar cal = Calendar.getInstance();
		eraseTime(cal);
		cal.set(Calendar.YEAR, annee);
		cal.set(Calendar.MONTH, mois);
		date = cal.getTime();
	}// constructeur année mois
	
	/** Supprime le jour du mois (fixé à 1), les heures, minutes, secondes et
	 * millisecondes du calendrier spécifié.
	 * 
	 * @param cal	Le calendrier à modifier.
	 */
	private void eraseTime(Calendar cal) {
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}// eraseTime
	
	/** Renvoie le premier jour du mois. */
	public Date getFirstDay() {
		return date;
	}// getFirstDay
	
	/** Renvoie l'année. */
	public int getYear() {
		return annee;
	}// getYear
	
	/** Renvoie le numéro du mois (dans l'année). */
	public int getNumInYear() {
		return mois;
	}// getNumInYear
	
	/** Renvoie le mois suivant. */
	public Month getNext() {
		return getTranslated(1);
	}// getNext
	
	/** Renvoie le mois précédent. */
	public Month getPrevious() {
		return getTranslated(-1);
	}// getPrevious
	
	/** Renvoie un mois décalé par rapport à celui-ci.
	 * 
	 * @param n	Le nombre de mois entre celui-ci et le mois à renvoyer. Par
	 * 			exemple si <code>n</code> est égal à 1, la méthode renvoie le
	 * 			mois suivant, s'il est égal à -2 elle renvoie l'avant-dernier
	 * 			mois avant celui-ci, etc.
	 * 
	 * @return	Une nouvelle instance.
	 */
	public Month getTranslated(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, n);
		return new Month(cal.getTimeInMillis());
	}// getTranslated
	
	
	// Comparaisons avec les objets Date
	
	/** Détermine si la date spécifiée se trouve dans ce mois calendaire.
	 * 
	 * @param date	La date à examiner.
	 * 
	 * @return		<code>true</code> si l'année et le mois de <code>date</code>
	 * 				correspondent à ceux de cet objet.
	 */
	public boolean includes(Date date) {
		if (date == null)
			return false;
		
		// Extraire le mois et l'année de cette date
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return annee == cal.get(Calendar.YEAR)
				&& mois == cal.get(Calendar.MONTH) + 1;
	}// includes

	/** Détermine si ce mois est strictement postérieur à la date spécifiée.
	 * 
	 * @param date	La date à examiner.
	 * 
	 * @return		<code>true</code> si <code>date</code> fait partie d'un mois
	 * 				calendaire strictement antérieur à ce objet. 
	 */
	public boolean after(Date date) {
		return !includes(date) && this.date.after(date);
	}// after Date
	
	/** Détermine si ce mois est strictement antérieur à la date spécifiée.
	 * 
	 * @param date	La date à examiner.
	 * 
	 * @return		<code>true</code> si <code>date</code> fait partie d'un mois
	 * 				calendaire strictement postérieur à ce objet. 
	 */
	public boolean before(Date date) {
		return !includes(date) && this.date.before(date);
	}// before Date
	
	
	// Comparaison avec les autres instances Month
	
	/** Détermine si ce mois est strictement postérieur au mois spécifié.
	 * 
	 * @param month	Le mois à comparer.
	 * 
	 * @return		<code>true</code> si ce mois est postérieur à
	 * 				<code>month</code>, <code>false</code> s'il est antérieur ou
	 * 				égal.
	 */
	public boolean after(Month month) {
		return annee > month.annee
				|| (annee == month.annee && mois > month.mois);
	}// after Month
	
	/** Détermine si ce mois est strictement antérieur au mois spécifié.
	 * 
	 * @param month	Le mois à comparer.
	 * 
	 * @return		<code>true</code> si ce mois est antérieur à
	 * 				<code>month</code>, <code>false</code> s'il est antérieur ou
	 * 				égal.
	 */
	public boolean before(Month month) {
		return annee < month.annee
				|| (annee == month.annee && mois < month.mois);
	}// before Month

	@Override
	public int compareTo(Month month) {
		if (after(month)) {
			return 1;
		} else if (before(month)) {
			return -1;
		} else {
			return 0;
		}
	}// compareTo Month
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Month) {
			Month m = (Month) o;
			return annee == m.annee && mois == m.mois;
		} else {
			return false;
		}// if Month
	}// equals
	
	@Override
	public int hashCode() {
		return (mois*113 + annee)*97;
	}// hashCode
	
	@Override
	public String toString() {
		return DF.format(date);
	}// toString
}
