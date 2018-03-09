/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.Iterator;

/** Un itérateur en lecture seule, c'est-à-dire sans la méthode
 * {@link java.util.Iterator#remove()}.
 * 
 * @author Olivier HAAS
 *
 * @param <T>
 */
public abstract class ReadOnlyIterator<T> implements Iterator<T> {

	/** @throws	UnsupportedOperationException */
	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}// remove
}
