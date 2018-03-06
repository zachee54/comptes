package haas.olivier.diagram;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/** Un dessinateur de la partie centrale d'un diagramme.
 * 
 * @author Olivier HAAS
 */
public interface Painter {

	/** Dessine la partie principale du diagramme.
	 * 
	 * @param g			Le contexte graphique.
	 * @param bounds	Le rectangle dans lequel dessiner le diagramme.
	 */
	void paintDiagram(Graphics2D g, Rectangle bounds);
	
	/** Dessine un paerçu du tracé de la série
	 * 
	 * @param serie	La série dont on veut un aperçu.
	 */
	void paintSample(Serie serie, Graphics2D g, Rectangle bounds);
	
	/** Renvoie le texte à afficher en infobulle.
	 * 
	 * @param p			Le point où se trouve le curseur de la souris
	 * @param bounds	Les dimensions actuelles du graphique.
	 * 
	 * @return			Un texte, éventuellement formaté en HTML, ou
	 * 					<code>null</code> s'il n'y a rien de pertinent à
	 * 					afficher.
	 */
	String getToolTipText(Point p, Rectangle bounds);
	
	/** Renvoie le modèle de données du diagramme. */
	DiagramModel getModel();
	
	/** Renvoie l'échelle du diagramme. */
	Echelle getEchelle();
}
