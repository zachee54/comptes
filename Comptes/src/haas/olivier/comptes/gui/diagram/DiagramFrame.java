package haas.olivier.comptes.gui.diagram;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import haas.olivier.diagram.DiagramAndAxisComponent;
import haas.olivier.diagram.DiagramModel;
import haas.olivier.diagram.Painter;
import haas.olivier.diagram.Legend;

/** Une fenêtre contenant un diagramme.
 * <p>
 * L'intégration dans une fenêtre à part permet de proposer en même temps des
 * options pour modifier le diagramme.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class DiagramFrame extends JFrame {

	/** Le panneau de sélection des séries de données */
	private Component seriesSelector;
	
	/** Construit et affiche une fenêtre contenant un diagramme.
	 * 
	 * @param diagramAndAxisComponent
	 * 				Le composant contenant le diagramme à afficher avec son
	 * 				contexte (étiquettes des axes).
	 */
	public DiagramFrame(DiagramAndAxisComponent diagramAndAxisComponent) {
		getContentPane().setBackground(Color.white);
		
		// Ajouter le diagramme à la fenêtre
		add(diagramAndAxisComponent);
		
		// Ajouter un panneau d'options générales en bas
		JPanel options = new JPanel(new BorderLayout());
		add(options, BorderLayout.SOUTH);
		
		// Ajouter la légende à droite
		Painter painter =
				diagramAndAxisComponent.getMainZone().getPainter();
		add(new Legend(painter), BorderLayout.EAST);
		
		// Définir le panneau d'options des séries
		DiagramModel model = painter.getModel();
		seriesSelector = new SeriesSelector(model);		
		
		// Ajouter un bouton afficant le sélecteur de séries
		JButton seriesButton = new JButton(new MenuIcon(15));
		seriesButton.setContentAreaFilled(false);
		seriesButton.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		seriesButton.addActionListener(new SelectorActionListener());
		options.add(seriesButton, BorderLayout.EAST);
		
		// Ajouter un slider pour régler la période d'affichage
		options.add(new TimeSelector(model));
		
		// Afficher
		setPreferredSize(new Dimension(800,600));// TODO ?
		pack();
		setVisible(true);
	}// constructeur
	
	/** Un <code>ActionListener</code> qui permet d'afficher ou de masquer le
	 * sélecteur de séries.
	 *
	 * @author Olivier Haas
	 */
	private class SelectorActionListener implements ActionListener {
		
		/** Le lieu où insérer le composant. Il s'agit d'une constante de
		 * <code>BorderLayout</code>.
		 */
		private final String orientation = BorderLayout.EAST;
		
		/** Le composant à afficher à la place du panneau lorsque celui-ci
		 * n'est pas affiché.
		 */
		private Component old;

		/** Affiche ou masque le panneau de sélection des séries de données.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (seriesSelector.getParent() == null) {	// Pas affiché ?
				
				// Mémoriser l'élément éventuellement affiché
				LayoutManager layout = getContentPane().getLayout();
				if (layout instanceof BorderLayout) {
					old = ((BorderLayout) layout).getLayoutComponent(
							orientation);
					remove(old);
				}
				
				// Afficher le panneau
				add(seriesSelector, orientation);
				
			} else {									// Déjà affiché ?
				
				// Enlever le panneau
				remove(seriesSelector);
				
				// Remettre l'ancien composant
				if (old != null)
					add(old, orientation);
			}// if
			revalidate();
			repaint();
		}// actionPerformed
		
	}// private inner class SelectorActionListener
}// class DiagramFrame