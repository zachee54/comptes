/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.actions;

import haas.olivier.util.Month;
import haas.olivier.util.Observable;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Un observable de changements de mois ou de dates. Bien que les changements de
 * données impactent toute l'application, chaque instance a ses propres
 * observateurs. Cela permet de ne mettre à jour que les observateurs dont on a
 * besoin. En contrepartie, il faut penser à mettre à jour les autres avant de
 * s'en servir.
 * 
 * @author Olivier Haas
 */
public class MonthObservable extends Observable<MonthObserver> {

	/**
	 * Le mois sélectionné. Par défaut, le mois en cours. Ce mois est le même
	 * pour toute l'application.
	 */
	private static Month month = Month.getInstance();

	/**
	 * La date sélectionnée. Par défaut, aucune date (juste un mois). Cette date
	 * est la même pour toute l'application.
	 */
	private static Date date = null;

	/**
	 * @return Le mois actuellement sélectionné.
	 */
	public static Month getMonth() {
		return month;
	}

	/**
	 * @return La date actuellement sélectionnée, ou null si la sélection porte
	 *         sur un mois sans précision du jour.
	 */
	public static Date getDate() {
		return date;
	}

	/**
	 * Notifie aux observateurs le mois actuel.
	 */
	private void notifyMonth() {
		for (MonthObserver observer : observers) {
			observer.monthChanged(month); // Notifier le mois
		}
	}

	/**
	 * Notifie aux observateurs la date actuelle.
	 */
	private void notifyDate() {
		try {
			for (MonthObserver observer : observers)
				observer.dateChanged(date);	// Notifier la date
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE,
					"Erreur pendant la lecture des données à la date choisie",
					e);
		}
	}

	/**
	 * Notifie tous les observateurs en leur indiquant le mois ou la date
	 * actuelle, sans les modifier. Utile pour forcer les observateurs à se
	 * mettre à jour.
	 */
	public void notifyObservers() {

		// Si une date est sélectionnée
		if (date != null) {
			notifyDate();			// Notifier la date
		} else {
			notifyMonth();			// Notifier le mois
		}
	}

	/**
	 * Modifie le mois sélectionné et notifie les observateurs.
	 */
	public void setMonth(Month newMonth) {
		month = newMonth;			// Nouveau mois
		date = null;				// Supprimer la date s'il y en a une
		notifyMonth();				// Notifie le nouveau mois
	}

	/**
	 * Modifie la date sélectionnée et notifie les observateurs.
	 */
	public void setDate(Date newDate) {
		date = newDate;						// Nouvelle date
		month = Month.getInstance(newDate); // Ça paraît bête, mais certains...
		notifyDate();						// Notifier les observateurs
	}
}
