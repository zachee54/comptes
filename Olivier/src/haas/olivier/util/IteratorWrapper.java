/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.Iterator;

/**
 * Un itérateur qui génère des valeurs à partir de celles d'un autre itérateur.
 * <p>
 * Idéalement, il aurait fallu séparer les fonctionnalités de
 * {@link haas.olivier.util.ReadOnlyIterator} et celles-ci, mais ce n'est pas
 * possible puisque Java n'autorise pas les héritages multiples.
 *
 * @author Olivier HAAS
 * 
 * @param <T>	Le type de valeurs renvoyées par cet itérateur.
 * 
 * @param <U>	Le type de valeurs renvoyées par l'itérateur délégué.
 */
public abstract class IteratorWrapper<T, U> extends ReadOnlyIterator<T> {

	/**
	 * L'itérateur délégué.
	 */
	private final Iterator<U> it;
	
	/**
	 * Construit un itérateur générant des valeurs à partir des valeurs d'un
	 * itérateur délégué.
	 * 
	 * @param it	L'itérateur délégué.
	 */
	public IteratorWrapper(Iterator<U> it) {
		this.it = it;
	}
	
	@Override
	public final boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public final T next() {
		return getValue(it.next());
	}
	
	/**
	 * Génère une valeur à renvoyer à partir de la valeur de l'itérateur
	 * délégué.
	 * 
	 * @param u	La valeur de l'itérateur délégué.
	 * 
	 * @return	La valeur que doit renvoyer cet itérateur, en fonction de
	 * 			<code>u</code>.
	 */
	protected abstract T getValue(U u);
}
