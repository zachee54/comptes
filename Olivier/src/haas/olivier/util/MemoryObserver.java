/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

/**
 * Un observateur de la mémoire heap disponible.<br>
 * Les objets qui implémentent cette interface sont susceptibles d'utiliser de
 * grandes quantités de mémoire et peuvent en libérer une partie sur demande, au
 * cas où la mémoire commencerait à saturer.
 *
 * @author Olivier HAAS
 */
public interface MemoryObserver {

	/**
	 * Lance une opération de délestage pour libérer de l'espace mémoire.
	 * 
	 * @return	<code>true</code> si de la mémoire a pu être libérée,
	 * 			<code>false</code> sinon.
	 */
	boolean deleste();
}
