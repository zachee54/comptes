/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.util.Month;

/**
 * Un collecteur d'écritures par dates dans un mois donné.
 *
 * @author Olivier Haas
 */
class EcrituresByDay {

	/**
	 * Les écritures par date.
	 */
	private final Map<Date,Collection<Ecriture>> map = new HashMap<>();
	
	/**
	 * Construit un collecteur d'écritures par dates.
	 * 
	 * @param month		Le mois au titre duquel on souhaite les écritures.
	 * @param pointages	<code>true</code> pour utiliser la date de pointage des
	 * 					écritures, <code>false</code> pour utiliser la date
	 * 					d'écriture.
	 * 
	 * @throws IOException 
	 */
	public EcrituresByDay(Month month, boolean pointages) throws IOException {
		EcritureDAO dao = DAOFactory.getFactory().getEcritureDAO();
		Iterable<Ecriture> ecritures = pointages
				? dao.getPointagesTo(month)
				: dao.getAllTo(month);
				
		for (Ecriture e : ecritures)
			addEcriture(e, pointages);
	}
	
	/**
	 * Ajoute une écriture à la collection.
	 * 
	 * @param e			L'écriture à ajouter.
	 * 
	 * @param pointages	<code>true</code> pour utiliser la date de pointage des
	 * 					écritures, <code>false</code> pour utiliser la date
	 * 					d'écriture.
	 */
	private void addEcriture(Ecriture e, boolean pointages) {
		Date date = pointages ? e.pointage : e.date;
		if (date == null)
			return;
		
		if (!map.containsKey(date))
			map.put(date, new ArrayList<>());
		
		map.get(date).add(e);
	}
	
	/**
	 * Renvoie les écritures d'une date.
	 * <p>
	 * Selon les arguments donnés au constructeur, il s'agit soit des dates
	 * d'écritures, soit des dates de pointages.
	 * 
	 * @param date	La date souhaitée
	 * @return		Une collection d'écritures, ou une collection vide s'il n'y
	 * 				a aucune écriture à cette date.
	 */
	public Collection<Ecriture> getEcritures(Date date) {
		Collection<Ecriture> ecritures = map.get(date);
		return ecritures == null ? Collections.emptyList() : ecritures;
	}
}
