package haas.olivier.diagram;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

/** Un diagramme représentant les données sous forme de courbes.
 * 
 * @author Olivier HAAS
 */
public class PainterCourbe extends AbstractPainter {
	
	/** Construit un diagramme en courbes.
	 * 
	 * @param model	Le modèle du diagramme.
	 */
	public PainterCourbe(DiagramModel model) {
		super(model);
	}// constructeur
	
	@Override
	public void paintDiagram(Graphics2D g, Rectangle bounds) {
		Echelle echelle = getEchelle();
		Object[] xValues = getModel().getXValues();
		Object xValue;									// Curseur abscisses
		Number yValue;									// Curseur ordonnées
		
		// Dessiner chaque série
		for (Serie serie : getModel().getSeries()) {
			g.setColor(serie.getColor());
			int n = 0;									// Compteur d'abscisses
			Path2D path = null;							// Chemin à tracer
			
			// Parcourir les données de la série
			for (int i=0; i<xValues.length; i++) {
				xValue = xValues[i];
				yValue = serie.get(xValue);
				
				// Selon qu'il y a ou non une valeur ici
				if (yValue == null) {					// Pas de valeur
					
					// Arrêter le chemin s'il y en a un en cours
					drawPath(g, path);					// Dessiner
					path = null;						// Et oublier
					
				} else {								// Une valeur
					
					// Coordonnée graphique horizontale
					int x = (n++) * bounds.width / (xValues.length - 1);
					
					// Commencer ou continuer le chemin
					if (path == null) {					// Pas de chemin
						path = new Path2D.Double();		// Commencer
						
						// Placer le premier point du chemin
						path.moveTo(
								x,
								echelle.getYFromValue(yValue, bounds.height));
						
					} else {							// Un chemin
						// Continuer le chemin
						path.lineTo(
								x,
								echelle.getYFromValue(yValue, bounds.height));
					}// if path
				}// if valeur y
			}// while x values
			
			// Tracer le chemin en cours à la dernière abscisse
			drawPath(g, path);
		}// for série
	}// paintDiagram
	
	/** Dessine le chemin spécifié.
	 * <p>
	 * Si <code>path==null</code>, la méthode ne fait rien.
	 * 
	 * @param g		Le contexte graphique.
	 * @param path	Le chemin à dessiner. Peut être <code>null</code>.
	 */
	private void drawPath(Graphics2D g, Path2D path) {
		if (path != null)
			g.draw(path);
	}// drawPath

	@Override
	public void paintSample(Serie serie, Graphics2D g, Rectangle bounds) {
		GraphicHints.prepareGraphics(g);
		g.setColor(serie.getColor());
		int y = bounds.y + bounds.height/2;
		g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
	}// paintSample

	@Override
	public String getToolTipText(Point p, Rectangle bounds) {
		Echelle echelle = getEchelle();
		
		// Calcul des abscisses entre lesquelles on se trouve
		Object[] xValues = getModel().getXValues();
		int gaps = bounds.width/(xValues.length - 2);	// Largeur d'intervalles
		int index1 = Math.min(p.x/gaps,					// Index juste à gauche
				xValues.length - 2);					// (Maxi avant-dernier)
		int x1 = index1 * gaps;							// Position de cet index
		int x2 = x1 + gaps;								// Position du suivant
		
		// Valeurs en abscisses à gauche et à droite du clic
		Object xValue1 = xValues[index1];
		Object xValue2 = xValues[index1 + 1];
		
		// Calcul de la série dont la ligne est la plus proche
		int dist = Integer.MAX_VALUE;					// Distance par défaut
		Serie serie = null;								// Série la plus proche
		for (Serie s : getModel().getSeries()) {
			
			// Trouver les valeurs de la série à droite et à gauche
			Number yValue1 = s.get(xValue1);			// Valeur à gauche
			Number yValue2 = s.get(xValue2);			// Valeur à droite
			
			// Selon que ces valeurs sont définies ou sont nulles
			int d;										// Carré de la distance
			if (yValue1 == null && yValue2 == null) {	// Aucune des deux
				continue;								// Laisser tomber
				
			} else if (yValue1 == null) {				// Seulement à droite
				int y = echelle.getYFromValue(			// Ordonnée à droite
						yValue2, bounds.height);
				d = (p.x-x2)*(p.x-x2)+(p.y-y)*(p.y-y);
				
			} else if (yValue2 == null) {				// Seulement à gauche
				int y = echelle.getYFromValue(			// Ordonnée à gauche
						yValue1, bounds.height);
				d = (p.x-x1)*(p.x-x1)+(p.y-y)*(p.y-y);
				
			} else {									// Deux valeurs
				int y1 = echelle.getYFromValue(			// Ordonnée à gauche
						yValue1, bounds.height);
				int y2 = echelle.getYFromValue(			// Ordonnée à droite
						yValue2, bounds.height);
				// Produit vectoriel 
				int pVect = Math.abs((p.x-x1)*(y1-y2)-(x1-x2)*(p.y-y1));
				// au carré, divisé par le carré de la distance entre les 2 pts
				d = pVect*pVect/((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
			}// if valeurs définies ou non
			
			// Tester si cette distance est plus petite que les précédentes
			if (d < dist) {
				dist = d;
				serie = s;
			}// if
		}// for série
		
		// Valeur à afficher: celle qui est non null, ou abscisse la plus proche
		Object xValue;									// Valeur abscisse
		Number yValue;									// Valeur ordonnée
		Number yValue1 = serie.get(xValue1);			// La valeur à gauche
		Number yValue2 = serie.get(xValue2);			// La valeur à droite
		if (yValue1 != null && (yValue2 == null || p.x-x1 < x2-p.x) ) {
			xValue = xValue1;
			yValue = yValue1;
			
		} else {
			xValue = xValue2;
			yValue = yValue2;
		}// if valeur non null la plus proche
		
		return "<html>" + serie + "<br/>" + xValue + " : " + yValue
				+ "</html>";
	}// getTooltipText MouseEvent
}
