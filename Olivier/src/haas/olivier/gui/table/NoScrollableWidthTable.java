/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import java.awt.Dimension;

/** Une <code>JTable</code> qui conserve sa largeur préférée même à l'intérieur
 * d'un <code>Viewport</code>, sans essayer de s'ajuster à la largeur du
 * <code>Viewport</code>.
 * <p>
 * Cela peut être très utile comme <code>rowHeader</code> dans un
 * <code>JScrollPane</code>.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class NoScrollableWidthTable extends SmartTable {
	
	/** Renvoie la dimension préférée (qui correspond à la largeur
	 * éventuellement modifiée par l'utilisateur) plutôt que la taille
	 * "scrollable" que l'implémentation mère définit arbitrairement.<br>
	 * Cela suffit pour faire en sorte qu'un viewport englobant s'ajuste
	 * automatiquement en largeur.
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}// getPreferredScrollableViewportSize
}
