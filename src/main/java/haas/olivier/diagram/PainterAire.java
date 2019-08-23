/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.Collections;

public class PainterAire extends AbstractPainter {
	
	/** Construit un dessinateur de diagramme sous forme d'aires empilées.
	 * 
	 * @param model	Le modèle de diagramme.
	 */
	PainterAire(DiagramModel model) {
		
		// Imposer une vue cumulée pour le calcul de l'échelle
		super(model, new Echelle(model.getAggregateView()));
	}// constructeur
	
	/** Calcule l'abscisse dans la fenêtre graphique, à partir de l'index de
	 * l'abscisse selon le modèle.
	 * 
	 * @param index	Index de l'abscisse selon le modèle.
	 * @param count	Nombre total d'abscisses selon le modèle.
	 * @param width	Largeur de la zone graphique.
	 * 
	 * @return		L'abscisse à utiliser dans la zone graphique.
	 */
	private static int getXCoordinate(int index, int count, int width) {
		return index * width / (count - 1);
	}// getXCoordinate

	@Override
	public void paintDiagram(Graphics2D g, Rectangle bounds) {
		
		// Une série sans valeurs à utiliser comme série de départ
		Serie previous = new Serie(0, null, null, false,
				Collections.<Object,Number>emptyMap());
		
		for (Serie serie : getModel().getAggregateView().getSeries()) {
			paintSerie(serie, previous, bounds, g);
			previous = serie;
		}// for série
	}// paintDiagram
	
	/** Dessine l'aire d'une série.
	 * 
	 * @param aggregate	La série à tracer, contenant des valeurs cumulées.
	 * @param previous	La série précédente (pour la partie inférieure de l'aire
	 * 					à tracer).
	 * @param bounds	Le rectangle dans lequel est dessiné le diagramme.
	 * @param g			Le contexte graphique.
	 */
	private void paintSerie(Serie aggregate, Serie previous, Rectangle bounds,
			Graphics2D g) {
		Echelle echelle = getEchelle();
		
		int height = bounds.height;
		int width = bounds.width;
		
		Object[] xValues = getModel().getXValues();
		int xCount = xValues.length;
		
		// Point de départ du polygone (en bas à gauche)
		Path2D path = new Path2D.Double();
		path.moveTo(
				0,
				echelle.getYFromValue(
						previous.get(xValues[0]), height));
		
		// Définir la limite supérieur de l'aire
		for (int i=0; i<xCount; i++) {
			path.lineTo(
					getXCoordinate(i, xCount, width),
					echelle.getYFromValue(
							aggregate.get(xValues[i]), height));
		}
		
		// Définir la limite inférieure, de droite à gauche
		for (int i=xCount-1; i>=0; i--) {
			path.lineTo(
					getXCoordinate(i, xCount, width),
					echelle.getYFromValue(
							previous.get(xValues[i]), height));
		}
		
		path.closePath();	// Par sécurité, parce normalement c'est inutile
		
		// Tracer le contour
		Color serieColor = aggregate.getColor();
		g.setColor(serieColor);
		g.draw(path);
		
		// Remplir l'intérieur avec -20% d'opacité
		g.setColor(new Color(
				serieColor.getRed(),
				serieColor.getGreen(),
				serieColor.getBlue(),
				(int) (serieColor.getAlpha() * 0.8)));
		g.fill(path);
	}// paintSerie
	
	@Override
	public void paintSample(Serie serie, Graphics2D g, Rectangle bounds) {
		
		// Intérieur
		GraphicHints.prepareGraphics(g);
		g.setColor(serie.getColor());
		g.fill(bounds);
		
		// Contour
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.0f));
		g.draw(bounds);
	}// paintSample

	@Override
	public String getToolTipText(Point p, Rectangle bounds) {
		Object[] xValues = getModel().getXValues();
		
		if (p.x >= bounds.width)
			return null;
		
		// Index non arrondi, proportionnel à l'abscisse du pointeur graphique
		// NB: Point.getX() renvoie un double, donc le résultat est un double
		double unroundedIndex = p.getX() * (xValues.length-1) / bounds.width;
		
		// Les index des valeurs à utiliser pour la suite
		// NB: roundedIndex est égal soit à index1, soit à index2
		int roundedIndex = (int) Math.rint(unroundedIndex);	// Index le + proche
		int index1 = (int) Math.floor(unroundedIndex);		// Index juste avant
		int index2 = index1 + 1;							// Index juste après
		
		/* Rechercher les aires qui recoupent le pointeur de souris.
		 * On observe les lignes qui passent entre les abscisses index1 et
		 * index2, et on regarde quand elles franchissent le point à l'abscisse
		 * x.
		 * S'il y a des séries avec des valeurs négatives, il peut y avoir
		 * plusieurs aires qui se recoupent. 
		 */
		int y = p.y;					// Ordonnée du pointeur
		double y1, y2 = Double.MAX_VALUE;// Ordonnée avant et après chaque série
		String result = "";
		for (Serie aggregate : getModel().getAggregateView().getSeries()) {
			y1 = y2;
			
			/* Un calcul de barycentre pour savoir où la ligne coupe l'abscisse
			 * du curseur.
			 * NB : la somme des coefs = index2-index1 = 1 par hypothèse
			 */
			y2 = getEchelle().getYFromValue(
					aggregate.get(xValues[index1]), bounds.height)
					* (index2 - unroundedIndex)
				+ getEchelle().getYFromValue(
						aggregate.get(xValues[index2]), bounds.height)
					* (unroundedIndex - index1);
			
			// Si la ligne a franchi le curseur, la souris est dans cette aire
			if ( (y1 < y) != (y2 < y) ) {
				Object xValue = xValues[roundedIndex];
				if (!result.isEmpty())
					result += "<br />";
				result += aggregate + " " + xValue + " : "
					+ getSerie(aggregate.id).get(xValue);
			}// if
		}// for serie
		
		return result.isEmpty()
				? null
				: "<html>" + result + "</html>";
	}// getToolTipText
	
	/** Renvoie la série non cumulée portant l'identifiant spécifié.
	 * 
	 * @param id	L'identifiant de la série à rechercher.
	 * @return		La série non cumulée portant l'identificant <code>id</code>
	 * 
	 * @throws IllegalArgumentException
	 * 				Si le modèle ne contient pas de série portant cet
	 * 				identifiant.
	 */
	private Serie getSerie(int id) {
		for (Serie serie : getModel().getSeries()) {
			if (serie.id == id)
				return serie;
		}
		
		throw new IllegalArgumentException(
				"Pas de série portant l'identifiant " + id);
	}// getSerie
}
