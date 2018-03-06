package haas.olivier.diagram;

import java.awt.Component;

/** Un contexte général pour les diagrammes.
 * <p>
 * Il permet de faire le lien entre les différents éléments essentiels d'un
 * diagramme.
 * 
 * @author Olivier HAAS
 */
public interface DiagramContext {

	/** Renvoie le modèle de données. */
	DiagramModel getModel();
	
	/** Renvoie une zone de diagramme. */
	DiagramComponent getZone();
	
	/** Renvoie l'échelle du diagramme. */
	Echelle getEchelle();
	
	/** Renvoie le dessinateur d'étiquettes de l'axe des abscisses. */
	DiagramXPainter getXPainter();
	
	/** Renvoie le dessinateur d'étiquettes de l'axe des ordonnées. */
	DiagramYPainter getYPainter();
	
	/** Renvoie la légende. */
	Component getLegend();
}
