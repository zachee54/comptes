/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.util;

import haas.olivier.gui.IconLoader;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/** Une action qui permet de copier un texte vers le presse-papiers.
 * <p>
 * Le texte à copier est paramétrable via une méthode abstraite.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public abstract class ActionCopy extends AbstractAction {
	
	private static final ImageIcon ICON_COPY = IconLoader.createImageIcon(
			ActionCopy.class, "../images/sc_copy.png", null);
	
	/** Construit une action qui copie un texte vers le presse-papiers.
	 * <p>
	 * L'action utilise une icône "Copier" imposée.
	 * 
	 * @param description	La description de l'action.
	 */
	public ActionCopy(String description) {
		super(description, ICON_COPY);
	}// constructeur
	
	/** Renvoie le texte à copier vers le presse-papiers. */
	protected abstract String getTextToCopy();

	/** Copie le texte renvoyé par <code>getTextToCopy()</code> vers le
	 * presse-papiers.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Contenu texte 
		StringSelection content = new StringSelection(getTextToCopy());
		
		// Placer dans le presse-papiers
		Toolkit.getDefaultToolkit().getSystemClipboard()
		.setContents(content, content);
	}// actionPerformed

}
