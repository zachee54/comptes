/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

/**
 * Une interface qui écoute les changements de dossier actif.
 * <p>
 * Date: 22 janv. 2018
 * @author Olivier HAAS
 * @see {@link SkeletonGUI}
 */
public interface DossierSelectionObserver<D extends Dossier> {

	/**
	 * Reçoit la notification de sélection/désélection d'un dossier dans
	 * l'interface graphique.
	 *
	 * @param newDossier	Le nouveau dossier sélectionné. Peut être
	 * 						<code>null</code>.
	 * 
	 * @param oldDossier	Le dossier précédemment sélectionné. Peut être
	 * 						<code>null</code>.
	 */
	void dossierChanged(D newDossier, D oldDossier);
}
