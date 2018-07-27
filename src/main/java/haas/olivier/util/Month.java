/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Un mois calendaire (mois et année).
 * <p>
 * Il s'agit d'objets immuables.
 * 
 * @author Olivier HAAS
 */
public class Month implements Comparable<Month>, Serializable {
	private static final long serialVersionUID = 4137756502743120255L;

	/**
	 * Le pool des instances existantes.
	 */
	private static final Pool instances = new Pool();
	
	/**
	 * Le format d'affichage.
	 */
	private final DateFormat format = new SimpleDateFormat("MMMM yyyy");
	
	/**
	 * La date sous-jacente, fixée au premier jour du mois.
	 */
	private final Date date;
	
	/**
	 * L'année.
	 */
	private final int annee;
	
	/**
	 * Le numéro du mois, de 1 à 12.
	 */
	private final int mois;
	
	/**
	 * Construit un mois calendaire correspondant au temps du calendrier
	 * spécifié.
	 *
	 * @param calendar	Le calendrier.
	 */
	private Month(Calendar calendar) {
		date = calendar.getTime();
		annee = calendar.get(Calendar.YEAR);
		mois = calendar.get(Calendar.MONTH) + 1;	// Mois de 1 à 12
	}

	/**
	 * Renvoie le mois calendaire actuel.
	 *
	 * @return	L'instance correspondant au mois actuel.
	 */
	public static Month getInstance() {
		return getInstance((Date) null);
	}
	
	/**
	 * Renvoie le mois calendaire correspondant à la date spécifiée.
	 *
	 * @param date	Une date.
	 * @return		Le mois calendaire correspondant à la date.
	 */
	public static Month getInstance(Date date) {
		Calendar calendar = Calendar.getInstance();
		if (date != null) {
			calendar.setTime(date);
		}
		return getInstance(calendar);
	}
	
	/**
	 * Renvoie le mois correspondant à l'année et au numéro du mois spécifiés.
	 *
	 * @param annee	L'année.
	 * @param mois	Le numéro du mois, de 1 à 12.
	 * @return		Le mois correspondant à l'année et au numéro du mois.
	 */
	public static Month getInstance(int annee, int mois) {
		Calendar calendar = Calendar.getInstance();
		eraseTime(calendar);
		calendar.set(Calendar.YEAR, annee);
		calendar.set(Calendar.MONTH, mois - 1);	// Mois de 0 à 11 dans Calendar
		return getInstance(calendar);
	}
	
	/**
	 * Renvoie le mois calendaire correspondant au temps du calendrier spécifié.
	 *
	 * @param calendar	Un calendrier. Son temps est modifié pendant l'exécution
	 * 					de la méthode.
	 * 
	 * @return			Le mois calendaire correspondant au temps du calendrier.
	 */
	private static Month getInstance(Calendar calendar) {
		
		/* Créer un mois à cette date */
		eraseTime(calendar);
		Month month = new Month(calendar);
		
		/* Réutiliser une instance existante si possible */
		return instances.get(month);
	}
	
	/**
	 * Supprime le jour du mois (fixé à 1), les heures, minutes, secondes et
	 * millisecondes du calendrier spécifié.
	 * 
	 * @param cal	Le calendrier à modifier.
	 */
	private static void eraseTime(Calendar cal) {
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}
	
	/**
	 * Renvoie le premier jour du mois.
	 */
	public Date getFirstDay() {
		return date;
	}
	
	/**
	 * Renvoie l'année.
	 */
	public int getYear() {
		return annee;
	}
	
	/**
	 * Renvoie le numéro du mois (dans l'année).
	 * 
	 * @return	Le numéro du mois, entre 1 (janvier) et 12 (décembre).
	 */
	public int getNumInYear() {
		return mois;
	}
	
	/**
	 * Renvoie le mois suivant.
	 */
	public Month getNext() {
		return getTranslated(1);
	}
	
	/**
	 * Renvoie le mois précédent.
	 */
	public Month getPrevious() {
		return getTranslated(-1);
	}
	
	/**
	 * Renvoie un mois décalé par rapport à celui-ci.
	 * 
	 * @param n	Le nombre de mois entre celui-ci et le mois à renvoyer. Par
	 * 			exemple si <code>n</code> est égal à 1, la méthode renvoie le
	 * 			mois suivant, s'il est égal à -2 elle renvoie l'avant-dernier
	 * 			mois avant celui-ci, etc.
	 * 
	 * @return	Une nouvelle instance.
	 */
	public Month getTranslated(int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, n);
		return new Month(calendar);
	}
	
	
	// Comparaisons avec les objets Date
	
	/**
	 * Détermine si la date spécifiée se trouve dans ce mois calendaire.
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
				&& mois == cal.get(Calendar.MONTH) + 1;	// Mois de 1 à 12
	}

	/**
	 * Détermine si ce mois est strictement postérieur à la date spécifiée.
	 * 
	 * @param date	La date à examiner.
	 * 
	 * @return		<code>true</code> si <code>date</code> fait partie d'un mois
	 * 				calendaire strictement antérieur à ce objet. 
	 */
	public boolean after(Date date) {
		return !includes(date) && this.date.after(date);
	}
	
	/**
	 * Détermine si ce mois est strictement antérieur à la date spécifiée.
	 * 
	 * @param date	La date à examiner.
	 * 
	 * @return		<code>true</code> si <code>date</code> fait partie d'un mois
	 * 				calendaire strictement postérieur à ce objet. 
	 */
	public boolean before(Date date) {
		return !includes(date) && this.date.before(date);
	}
	
	
	// Comparaison avec les autres instances Month
	
	/**
	 * Détermine si ce mois est strictement postérieur au mois spécifié.
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
	}
	
	/**
	 * Détermine si ce mois est strictement antérieur au mois spécifié.
	 * 
	 * @param month	Le mois à comparer.
	 * 
	 * @return		<code>true</code> si ce mois est antérieur à
	 * 				<code>month</code>, <code>false</code> s'il est antérieur ou
	 * 				égal.
	 */
	public boolean before(Month month) {
		return (annee < month.annee)
				|| ((annee == month.annee) && (mois < month.mois));
	}

	@Override
	public int compareTo(Month month) {
		if (after(month)) {
			return 1;
		} else if (before(month)) {
			return -1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Month) {
			Month m = (Month) o;
			return (annee == m.annee) && (mois == m.mois);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (mois*113 + annee)*97;
	}
	
	@Override
	public String toString() {
		return format.format(date);
	}
}
