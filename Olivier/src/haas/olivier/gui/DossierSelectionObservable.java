/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 * 
 */
package haas.olivier.gui;

import haas.olivier.util.Observable;

/**
 * Un observable de changement de dossier actif dans l'interface graphique.
 * <p>
 * Date: 22 janv. 2018
 * @author Olivier HAAS
 */
public class DossierSelectionObservable<D extends Dossier>
extends Observable<DossierSelectionObserver<D>> {

	DossierSelectionObservable() {
	}
	
	/**
	 * Notifie le changement de dossier aux observateurs.
	 *
	 * @param newDossier	Le nouveau dossier sélectionné. Peut être
	 * 						<code>null</code>.
	 * 
	 * @param oldDossier	Le dossier précédemment sélectionné. Peut être
	 * 						<code>null</code>.
	 */
	void dossierChanged(D newDossier, D oldDossier) {
		for (DossierSelectionObserver<D> o : observers)
			o.dossierChanged(newDossier, oldDossier);
	}
}
