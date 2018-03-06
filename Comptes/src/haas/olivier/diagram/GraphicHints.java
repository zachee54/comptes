package haas.olivier.diagram;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Une classe utilitaire pour la configuration du mode graphique.
 * 
 * @author Olivier HAAS
 */
class GraphicHints {

	/** Ajuste des paramètres tels que l'antialiasing pour un meilleur rendu. */
	static void prepareGraphics(Graphics2D g) {
		g.setStroke(new BasicStroke(
				2.0f,
				BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	}// prepareGraphics
}
