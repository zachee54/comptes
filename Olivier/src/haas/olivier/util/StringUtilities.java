/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.Iterator;

/**
 * Une classe utilitaire pour le traitement des chaînes de caractères.
 * 
 * @author Olivier HAAS
 */
public class StringUtilities {

	private StringUtilities() {
	}
	
	/**
	 * Concatène des chaînes de texte en ajoutant une liaison entre deux
	 * valeurs.
	 * 
	 * @param glue		La liaison entre deux valeurs.
	 * @param values	Les valeurs à concaténer.
	 */
	public static String join(String glue, Iterable<String> values) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = values.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(glue);
			}
		}
		return sb.toString();
	}
}
