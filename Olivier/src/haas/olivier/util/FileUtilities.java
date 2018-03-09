/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.io.File;

/** Une classe utilitaire traitant les fichiers.
 * 
 * @author Olivier HAAS
 */
public class FileUtilities {

	/** Renvoie l'extension du fichier.
	 * 
	 * @param file	Le fichier dont il faut trouver l'extension.
	 * 
	 * @param point	<code>true si le point doit être inclus dans le résultat,
	 * 				<code>false</code> sinon.
	 * 
	 * @return		L'extension (avec ou sans point), ou une chaîne vide si le
	 * 				fichier n'a pas d'extension, c'est-à-dire s'il n'a pas
	 * 				point, ou si le point se trouve tout au début du fichier.
	 */
	public static String getExtension(File file, boolean point) {
		String name = file.getName();
		int i = name.lastIndexOf('.');				// Trouver le denier point
		if (i > 0 && i < name.length()) {			// Si au milieu ou à la fin
			return name.substring(
					(point ? i : i+1));				// Renvoyer la suite
		} else {
			return "";								// Sinon renvoyer vide
		}// if point au milieu ou à la fin
	}// getExtension
}
