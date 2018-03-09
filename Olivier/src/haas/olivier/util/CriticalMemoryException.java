/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

/** Une exception qui indique que la mémoire est dans une situation très
 * critique.<br>
 * Les classes qui reçoivent une telle exception sont censées faire échouer les
 * traitements trop gourmands en mémoire qu'elles auraient en cours.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class CriticalMemoryException extends Exception {
	
	/** Construit une exception indiquant que la mémoire vive est en état
	 * critique.
	 */
	CriticalMemoryException() {
		super("Mémoire vive critique");
	}// constructeur
}
