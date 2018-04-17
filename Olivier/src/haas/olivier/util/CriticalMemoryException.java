/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

/**
 * Une exception non vérifiée qui indique que la mémoire est dans une situation
 * très critique.<br>
 * Le risque de débordement mémoire est tel qu'en principe, le thread en cours
 * devrait s'arrêter immédiatement en laissant l'exception remonter la pile
 * d'exécution.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class CriticalMemoryException extends RuntimeException {
	
	/**
	 * Construit une exception indiquant que la mémoire vive est en état
	 * critique.
	 */
	CriticalMemoryException() {
		super("Mémoire vive critique");
	}
}
