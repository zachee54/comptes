/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 * 
 */
package haas.olivier.gui.table;

import javax.swing.table.TableModel;

/**
 * Un memento encapsulant une référence vers un sous-modèle de table et un
 * index dans ce sous-modèle.
 * <p>
 * Il s'agit d'un objet immuable.
 * <p>
 * Cette classe sert à implémenter la valeur de retour de certaines méthodes,
 * qui est par nature un tuple de deux valeurs de classes différentes. 
 * 
 * @author Olivier HAAS
 */
public class SubModelAndIndex {

	/**
	 * Le sous-modèle.
	 */
	public final TableModel model;
	
	/**
	 * L'index d'une ligne ou d'une colonne dans le sous-modèle.
	 */
	public final int index;
	
	/**
	 * Construit un objet gardant une référence vers un sous-modèle de table et
	 * un index.
	 * 
	 * @param model	Le sous-modèle de table.
	 * @param index	L'index d'une ligne ou d'une colonne dans
	 * 				<code>model</code>.
	 */
	public SubModelAndIndex(TableModel model, int index) {
		this.model = model;
		this.index = index;
	}
}