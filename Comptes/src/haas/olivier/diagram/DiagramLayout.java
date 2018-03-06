package haas.olivier.diagram;


import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/** Un agenceur de diagrammes.
 * <p>
 * On utilise un seul composant, qui est le
 * {@link haas.olivier.diagram.DiagramComponent DiagramZone}.<br>
 * On utilise également des
 * {@link haas.olivier.diagram.XLabels DiagramXPainter} et
 * {@link haas.olivier.diagram.YLabels DiagramYPainter}, qui ne sont pas
 * des <code>Component</code>s, et qui servent uniquement à compléter le rendu
 * graphique du diagramme pour l'affichage des étiquettes.
 * 
 * @author Olivier HAAS
 */
class DiagramLayout implements LayoutManager {
	
	/** Le composant graphique à agencer. */
	private final DiagramAndAxisComponent component;
	
	/** Construit un agenceur de diagramme.
	 * 
	 * @param component	Le composant graphique à agencer.
	 */
	public DiagramLayout(DiagramAndAxisComponent component) {
		this.component = component;
	}// constructeur
	
	@Override
	public void addLayoutComponent(String name, Component comp) {}

	@Override
	public void removeLayoutComponent(Component comp) {}

	@Override
	public void layoutContainer(Container parent) {
		DiagramComponent zone = component.getMainZone();
		XLabels xLabels = component.getXLabels();
		YLabels yLabels = component.getYLabels();
		Insets zoneInsets = zone.getInsets();
		
		// Les dimensions verticales utiles
		int absHeight = xLabels.getPreferredSize().height;
		int verticalOverflow = yLabels.getVerticalOverflow();
		int topOverflow = max(verticalOverflow, zoneInsets.top);
		int bottomOverflow = max(verticalOverflow, zoneInsets.bottom);
		
		// Les dimensions horizontales utiles
		int ordWidth = yLabels.getPreferredSize().width;
		int leftOverflow = xLabels.getLeftOverflow();
		int rightOverflow = xLabels.getRightOverflow();
		int leftMargin = max(ordWidth, leftOverflow);
		
		// Taille disponible au centre
		int zoneAvailableWidth = parent.getWidth() - leftMargin - rightOverflow;
		int zoneAvailableHeight =
				parent.getHeight() - absHeight - topOverflow - bottomOverflow;
		
		// Corriger si la zone dispo est trop grande (+) ou trop petite (-)
		Dimension minZoneSize = zone.getMinimumSize();
		Dimension maxZoneSize = zone.getMaximumSize();
		int xDelta = 0, yDelta = 0;
		xDelta = min(0, zoneAvailableWidth - minZoneSize.width)
				+ max(0, zoneAvailableWidth - maxZoneSize.width);
		yDelta = min(0, zoneAvailableHeight - minZoneSize.height)
				+ max(0, zoneAvailableHeight - maxZoneSize.height);
		
		int zoneWidth = zoneAvailableWidth - xDelta;
		int zoneHeight = zoneAvailableHeight - yDelta;
		int zoneX = leftMargin + xDelta/2;
		int zoneY = topOverflow - zoneInsets.top + yDelta/2;
		
		// Dimensions de chaque élément
		zone.setBounds(new Rectangle(
				zoneX,
				zoneY,
				zoneWidth,
				zoneHeight));
		
		yLabels.setBounds(new Rectangle(
				max(0, zoneX - ordWidth),
				zoneY + zoneInsets.top - verticalOverflow,
				min(zoneX, ordWidth),
				/* La première graduation est dessinée à la hauteur du dernier
				 * pixel visible, donc zoneY + zoneHeight - 1.
				 * On doit enlever 1 pixel pour être aligné avec elle.
				 */
				zoneHeight - 1 - zoneInsets.top - zoneInsets.bottom
				+ verticalOverflow*2));
		
		xLabels.setBounds(new Rectangle(
				zoneX + zoneInsets.left - leftOverflow,
				zoneY + zoneHeight - zoneInsets.bottom + bottomOverflow,
				leftOverflow + zoneWidth - zoneInsets.left - zoneInsets.right
				+ rightOverflow,
				absHeight));
	}// layoutContainer

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return component.getMainZone().getMinimumSize();
	}// minimumLayoutSize
	
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		DiagramComponent zone = component.getMainZone();
		return new Dimension(
				component.getYLabels().getPreferredSize().width
				+ zone.getPreferredSize().width,
				component.getXLabels().getPreferredSize().height
				+ zone.getPreferredSize().height);
	}// preferredLayoutSize
}

