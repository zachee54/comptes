/*
 * Copyright (c) 2018 DGFiP - Tous droits réservés
 * 
 */
package haas.olivier.info;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Un formateur de message en vue de l'affichage dans une boîte de dialogue.
 * <p>
 * Cette implémentation ajoute des balises HTML et force une largeur de texte de
 * 500 px, pour permettre un affichage agréable à l'oeil.
 *
 * @author Olivier HAAS
 */
class DialogFormatter extends Formatter {
	
	/**
	 * Formate le message spécifié au format HTML avec une largeur de 500 px.
	 */ 
	@Override
	public String format(LogRecord record) {
		String message = formatMessage(record).replace("\n", "<br>");
		return String.format("<html><p width=\"500\">%s</p></html>", message);
	}
}