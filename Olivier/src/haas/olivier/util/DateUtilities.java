/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Une classe utilitaire traitant les dates.
 * 
 * @author Olivier HAAS
 */
public class DateUtilities {
	
	private DateUtilities() {
	}
	
	/**
	 * Renvoie la date en fixant l'heure à minuit.<br>
	 * En pratique, il s'agit de supprimer les heures, minutes, secondes et
	 * millisecondes.
	 * 
	 * @param date	La date (en millisecondes depuis Epoch) dont on veut
	 * 				supprimer les heures, minutes, secondes et millisecondes.
	 * 
	 * @return		Une date fixée à minuit, le même jour que <code>date</code>.
	 */
	public static Date getDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
