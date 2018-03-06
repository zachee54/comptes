package haas.olivier.diagram;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/** Un agenceur des étiquettes des ordonnées.
 * 
 * @author Olivier Haas
 */
class YLabelsLayout implements LayoutManager {

	/** L'échelle de l'axe des ordonnées. */
	private final Echelle echelle;
	
	/** Construit un agenceur d'étiquettes de l'axe des ordonnées.
	 * 
	 * @param echelle	L'échelle de l'axe des ordonnées.
	 */
	public YLabelsLayout(Echelle echelle) {
		this.echelle = echelle;
	}// constructeur
	
	/** @return	La plus grande largeur préférée et la plus grande hauteur
	 * 			préférée parmi les composants.
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int width = 0;
		int height = 0;
		for (Component child : parent.getComponents()) {
			Dimension childPrefSize = child.getPreferredSize();
			width = Math.max(width, childPrefSize.width);
			height = Math.max(height, childPrefSize.height);
		}
		return new Dimension(width, height);
	}// preferredLayoutSize

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(0,0);
	}// minimumLayoutSize

	/** Positionne les étiquettes en fonction des graduations.<br>
	 * Si plusieurs étiquettes sont censées se recouper, seule la première
	 * d'entre elles est affichée.
	 * <p>
	 * Cette méthode suppose qu'il y a autant d'enfants à positionner que de
	 * graduations.
	 */
	@Override
	public void layoutContainer(Container parent) {
		Component[] children = parent.getComponents();
		int index = 0;
		Rectangle previousBounds = null;
		
		for (Number graduation : echelle.getGraduations()) {
			Component child = children[index++];
			Rectangle bounds =
					getBoundsForGraduation(parent, child, graduation);
			
			// Si cette étiquette ne recoupe pas la dernière affichée
			if (previousBounds == null || !previousBounds.intersects(bounds)) {
				child.setBounds(bounds);
				previousBounds = bounds;
				
			} else {
				// Recoupement : ne pas afficher (rectangle de taille nulle)
				child.setBounds(0, 0, 0, 0);
			}// if
		}// for
	}// layoutContainer
	
	/** Renvoie le rectangle dans lequel afficher l'étiquette correspondant à
	 * une graduation.
	 * <p>
	 * Le composant qui dessine les étiquettes des ordonnées est décalé vers le
	 * haut par rapport au diagramme, l'écart étant de la moitié de la hauteur
	 * d'une étiquette.<br>
	 * Ainsi, la distance entre le coin supérieur gauche du composant et le coin
	 * supérieur gauche de l'étiquette est la même qu'entre le coin supérieur
	 * gauche du diagramme et la graduation.<br>
	 * Autrement dit, on place les étiquettes, à l'intérieur de leur composant,
	 * à la même ordonnée que les graduations à l'intérieur du diagramme.<br>
	 * Compte tenu du décalage de départ, les étiquettes apparaissent centrées
	 * face à leur graduation.
	 * <p>
	 * Les ordonnées des étiquettes s'étalent de <code>0</code> à
	 * <code>parent.getHeight() - labelHeight</code>.
	 * 
	 * @param parent		Le parent.
	 * @param graduation	La graduation.
	 * 
	 * @return				Un rectangle centré verticalement sur la graduation,
	 * 						aligné à droite. La largeur et la hauteur sont les
	 * 						dimensions préférées de <code>label</code>, dans la
	 * 						limite de la largeur disponible.
	 */
	private Rectangle getBoundsForGraduation(Container parent,
			Component child, Number graduation) {
		int width = parent.getWidth();
		int height = parent.getHeight();
		double min = echelle.getMin();
		double max = echelle.getMax();
		double number = graduation.doubleValue();
		Dimension prefChildSize = child.getPreferredSize();
		
		// Abscisse le plus à droite possible
		int x = Math.max(0, width - prefChildSize.width);
		
		// Largeur et hauteur préférées dans la limite du possible
		int labelWidth = width - x;
		int labelHeight = prefChildSize.height;
		
		// Ordonnée proportionnelle à l'échelle
		double yCoeff = (max - number) / (max - min);
		int y = (int) ((height - labelHeight) * yCoeff);
		
		return new Rectangle(x, y, labelWidth, labelHeight);
	}// getBoundsForGraduation

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

}
