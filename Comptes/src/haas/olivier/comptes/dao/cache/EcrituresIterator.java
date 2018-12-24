/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

import haas.olivier.comptes.Ecriture;
import haas.olivier.util.ReadOnlyIterator;

/**
 * Un itérateur qui parcourt les écritures dans une collection à deux niveaux.
 * <p>
 * Cette classe est particulièrement utile pour parcourir les écritures, sachant
 * que <code>CacheEcritureDAO</code> stocke les écritures dans des collections à
 * deux niveaux.
 * 
 * @author Olivier HAAS
 */
class EcrituresIterator extends ReadOnlyIterator<Ecriture> {
	
	/**
	 * L'itérateur principal qui parcourt le premier niveau de la collection.
	 */
	private final Iterator<NavigableSet<Ecriture>> it1;
	
	/**
	 * L'itérateur de second niveau actuel.
	 */
	private Iterator<Ecriture> it2;
	
	/**
	 * Drapeau indiquant si les écritures doivent être parcourues dans l'ordre
	 * naturel (<code>true</code>) ou dans l'ordre inverse (<code>false</code>).
	 */
	private final boolean ordre;
	
	/**
	 * Construit un itérateur d'écritures à partir d'une collection à deux
	 * niveaux.
	 * <p>
	 * Les collections de deuxième niveau seront chacune parcourue en ordre
	 * inverse. Ainsi, 
	 * 
	 * @param it	Un <code>Iterator</code> dont les valeurs sont des
	 * 				collections d'écritures.
	 * 
	 * @param ordre	<code>true</code> si les écritures doivent être parcourues
	 * 				dans leur ordre naturel.
	 */
	public EcrituresIterator(Iterator<NavigableSet<Ecriture>> it,
			boolean ordre) {
		it1 = it;
		this.ordre = ordre;
	}

	@Override
	public boolean hasNext() {
		while (it2 == null || !it2.hasNext()) {	// Pas d'écriture suivante ?
			if (it1.hasNext()) {				// Essayer le lot suivant
				it2 = ordre
						? it1.next().iterator()				// Ordre naturel
						: it1.next().descendingIterator();	// Ordre inverse
			} else {
				return false;					// Pas de lot suivant = fini
			}
		}
		return true;
	}

	@Override
	public Ecriture next() {
		if (!hasNext())					// Repositionner le curseur si besoin
			throw new NoSuchElementException();
		return it2.next();				// Écriture suivante
	}
}