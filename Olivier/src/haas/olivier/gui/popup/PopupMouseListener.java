/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.popup;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

/**
 * Un gestionnaire d'évènements de souris sur plusieurs composants, qui filtre
 * les événements de type <code>mouseExited</code> lorsque la souris ne fait que
 * passer d'un composant à un autre avec chevauchement, parmi ceux enregistrés.
 * <p>
 * Cette classe est utilisée pour neutraliser l'événement de sortie de la souris
 * lorsque le pointeur passe d'un popup à son onglet ou inversement. Cela évite
 * un effet de flash.<br>
 * Pour cela, les deux composants doivent se chevaucher d'au moins 1 pixel. 
 * 
 * @author Olivier HAAS
 */
class PopupMouseListener extends MouseAdapter {

	/**
	 * Les composants enregistrés.
	 */
	private final Set<Component> components = new HashSet<Component>();
	
	/**
	 * L'animateur de popup.
	 */
	private final PopupAnimator animator;
	
	/**
	 * Construit un gestionnaire d'évènements de souris pour les popups et
	 * leurs onglet.
	 * 
	 * @param animator	L'animateur de popup.
	 */
	public PopupMouseListener(PopupAnimator animator) {
		this.animator = animator;
	}

	/**
	 * Enregistre un nouveau composant.
	 * 
	 * @param comp	Le nouveau composant à écouter.
	 */
	public void register(Component comp) {
		components.add(comp);
		comp.addMouseListener(this);
	}
	
	/**
	 * Désenregistre un composant.
	 * 
	 * @param comp	Le composant à ne plus écouter. Peut être <code>null</code>.
	 */
	public void unregister(Component comp) {
		components.remove(comp);
		if (comp != null)
			comp.removeMouseListener(this);
	}

	/**
	 * Affiche le popup.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		animator.showPopup();
	}

	/**
	 * Masque le popup, sauf si le composant qui a généré l'évènement chevauche
	 * à cet endroit un autre composant enregistré.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		
		// Ignorer l'événement à proximité dans un composant aussi enregistré
		Component source = (Component) e.getSource();
		for (Component comp : components) {
			if (comp == e.getSource())
				continue;
			
			Rectangle rect = comp.getBounds();
			Point p = SwingUtilities.convertPoint(
					source, e.getPoint(), comp.getParent());
			if (rect.contains(p))
				return;
		}
		
		animator.hidePopup();
	}
}
