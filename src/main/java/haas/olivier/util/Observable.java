/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Une classe abstraite commune pour les observables, permettant notamment de
 * centraliser la gestion de la collection des observateurs.
 *
 * @param <T>	Le type d'observateurs gérés par cet observable.
 *
 * @author Olivier HAAS
 */
public abstract class Observable<T> {

	/**
	 * La collection des observateurs.
	 */
	protected Set<T> observers = new HashSet<T>();
	
	/**
	 * Ajoute un observateur.
	 * 
	 * @param observer	Le nouvel observateur.
	 */
	public void addObserver(T observer) {
		observers.add(observer);
	}
	
	/**
	 * Retire un observateur.
	 * 
	 * @param observer	L'observateur à retirer.
	 */
	public void removeObserver(T observer) {
		observers.remove(observer);
	}
}
