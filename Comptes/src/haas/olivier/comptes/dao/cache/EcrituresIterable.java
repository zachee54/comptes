package haas.olivier.comptes.dao.cache;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;

import haas.olivier.comptes.Ecriture;

/**
 * Un <i>wrapper</i> pour utiliser des <code>EcrituresIterator</code> sous
 * l'interface d'un <code>Iterable</code>.
 *
 * @author Olivier HAAS
 */
class EcrituresIterable implements Iterable<Ecriture> {

	/**
	 * La collection à faire parcourir par l'itérateur des écritures. Il s'agit
	 * d'une collection à deux niveaux (une collection de collections).
	 */
	private final Iterable<NavigableSet<Ecriture>> coll;
	
	/**
	 * Drapeau indiquant si les écritures doivent être parcourues dans l'ordre
	 * naturel (<code>true</code>) ou dans l'ordre inverse (<code>false</code>).
	 */
	private final boolean ordre;
	
	/**
	 * Renvoie un objet contenant des écritures et pouvant être parcouru avec
	 * l'interface <code>Iterable</code>.
	 * <p>
	 * Les valeurs, tant au premier qu'au deuxième niveau, seront parcourues
	 * dans l'ordre inverse.
	 * 
	 * @param map	Une <code>Map</code> dont les valeurs sont des collections
	 * 				d'écritures.
	 * 
	 * @param ordre	<code>true</code> si les écritures et les valeurs de 1er
	 * 				niveau (généralement des mois) doivent être triées dans leur
	 * 				ordre naturel.
	 */
	public EcrituresIterable(NavigableMap<?, NavigableSet<Ecriture>> map,
			boolean ordre) {
		
		// Trier les mois dans l'ordre chronologique, ou l'ordre inverse
		coll = (ordre ? map : map.descendingMap()).values();
		this.ordre = ordre;
	}

	@Override
	public Iterator<Ecriture> iterator() {
		return new EcrituresIterator(coll.iterator(), ordre);
	}
	
}