/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.popup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Un panneau affichant un onglet de popup. Quand la souris passe sur l'onglet,
 * le popup s'affiche avec une animation.
 *
 * @author Olivier HAAS
 */
public class RootPaneWithPopup extends JRootPane implements ChangeListener {
	private static final long serialVersionUID = -8627296544844654563L;

	/**
	 * La couleur de fond de tous les popups.
	 */
	public static final Color POPUP_BACKGROUND = new Color(255, 255, 223);
	
	/**
	 * La couleur de bordure du popup et de son onglet.
	 */
	public static final Color BORDER_COLOR = Color.BLACK;
	
	/**
	 * L'épaisser de la borudre du popup et de son onglet.<br>
	 * Il faut qu'elle soit au moins égale à 1.
	 * <p>
	 * Pour des raisons esthétiques, le popup et l'onglet se chevauchent sur
	 * l'épaisseur de la bordure. Cela donne une impression de continuité entre
	 * l'intérieur du popup et l'intérieur de l'onglet (d'autant qu'ils ont la
	 * même couleur de fond) et entre leurs bordures.<br>
	 * Mais ce chevauchement est également important dans la gestion
	 * événementielle : si les composants sont juxtaposés sans chevauchement,
	 * le passage de la souris de l'un à l'autre provoque un événement
	 * {@link java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent) mouseExited}
	 * qui déclenche une animation, d'où un effet de flash.
	 */
	public static final int BORDER_THICKNESS = 1;
	
	/**
	 * Définit les composants qui servent de popup et d'onglet.
	 * 
	 * @param popup	Le composant servant de popup. Il sera masqué et pourra être
	 * 				affiché dynamiquement.<br>
	 * 				Il est recommandé que ce composant ne soit pas opaque, de
	 * 				manière à faire apparaître la couleur de fond propre aux
	 * 				popups.
	 * 
	 * @param tab	Le composant qui sert d'onglet. Il sera toujours affiché.
	 * 				<br> Il est recommandé que ce composant ne soit pas opaque,
	 * 				de manière à faire apparaître la couleur de fond propre aux
	 * 				popups.
	 */
	public void setPopup(JComponent popup, JComponent tab) {
		Component popupComponent = encapsulatePopup(popup);
		Component tabComponent = encapsulateTab(tab);
		
		/*
		 * Créer un glass pane avec ces deux composants.
		 * 
		 * Le JLayeredPane permet de gérer le chevauchement des deux composants
		 * tout en gardant l'onglet au-dessus du popup.
		 */
		JLayeredPane glass = new JLayeredPane();
		glass.setOpaque(false);
		glass.setLayout(new PopupLayout(this));
		glass.add(popupComponent, PopupLayout.POPUP_LAYER);
		glass.add(tabComponent, PopupLayout.TAB_LAYER);
		
		setGlassPane(glass);
		glass.setVisible(true);
	}
	
	/**
	 * Renvoie un nouveau panneau contenant le composant <code>popup</code>.
	 * <br>On lui ajoute une bordure en bas pour l'esthétique, et une bordure de
	 * 1 pixel sur les autres côtés avec la couleur {@link #BORDER_COLOR}. Cette
	 * bordure sur les trois autres côtés est importante pour détecter les
	 * évènements de souris lorsque la souris quitte le popup en passant par un
	 * de ces trois côtés.
	 * <p>
	 * Cette solution n'est pas parfaite parce que si la souris quitte trop
	 * rapidement le popup, elle peut "sauter" au-dessus de la bordure du
	 * <code>JPanel</code> sans être détectée.<br>
	 * La question s'est posée de ne pas encapsuler le popup et de lui ajouter
	 * directement la bordure inférieure esthétique. Mais cela posait plusieurs
	 * problèmes :<ul>
	 * <li>	en cas de réutilisation du popup, il conservait la bordure ;
	 * <li>	si l'implémentation ajoute la bordure à l'existante, alors cela pose
	 * 		problème si le même composant est ajouté plusieurs fois comme popup
	 * 		(il a plusieurs bordures inférieures juxtaposées) ; si
	 * 		l'implémentation prévoit au contraire de remplacer la bordure
	 * 		existante, alors on perd la bordure éventuellement définie par
	 * 		l'utilisateur ;
	 * <li>	si le popup est un <code>JPanel</code> contenant un composant comme
	 * 		un <code>JButton</code>, alors quand la souris passe sur le
	 * 		<code>JButton</code> elle est considérée comme sortant du
	 * 		<code>JPanel</code>, et donc le popup se referme...
	 * </ul>
	 * 
	 * @param popup	Le composant popup.
	 * @return		Un nouveau panneau.
	 */
	private Component encapsulatePopup(JComponent popup) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(POPUP_BACKGROUND);
		panel.add(popup);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(
						0, 0, BORDER_THICKNESS, 0, BORDER_COLOR),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		return panel;
	}
	
	/**
	 * Renvoie un nouveau panneau contenant l'onglet <code>tab</code>, avec un
	 * paramétrage approprié.
	 * 
	 * @param tab	Le composant servant d'onglet.
	 * @return		Un nouveau panneau.
	 * 
	 * @see {@link TabBorder}
	 */
	private Component encapsulateTab(JComponent tab) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new TabBorder());
		panel.setOpaque(false);		// La bordure remplit en fait tout le fond
		panel.add(tab);
		return panel;
	}

	/**
	 * Reçoit les notifications de changements sur le glass pane. Typiquement,
	 * il s'agit du déplacement du popup et de son onglet lors d'une animation
	 * consistant à afficher ou masquer le popup.
	 * <p>
	 * La méthode revalide le glass pane et redessine le panneau.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		((JComponent) getGlassPane()).revalidate();
		repaint();
	}
}
