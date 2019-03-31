/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/** Un <code>JComponent</code> repréentant la zone graphique d'un diagramme.
 * 
 * @author Olivier HAAS
 */
public class DiagramComponent extends JComponent
implements DiagramModelObserver {
	private static final long serialVersionUID = -4699134755056988055L;

	/** Le dessinateur de diagramme. */
	private Painter painter;
	
	/** Construit un composant représentant la zone graphique d'un diagramme.
	 * 
	 * @param painter	Le dessinateur de la zone du diagramme.
	 */
	public DiagramComponent(Painter painter) {
		this.painter = painter;
		painter.getModel().getObservable().addObserver(this);
		
		ToolTipManager.sharedInstance().registerComponent(this);
	}// constructeur
	
	@Override
	public void diagramChanged() {
		revalidate();
		repaint();
	}// dataChanged
	
	@Override
	public String getToolTipText(MouseEvent e) {
		Point p = e.getPoint();
		Insets insets = getInsets();
		Point painterPoint = new Point(p.x - insets.left, p.y - insets.top);
		return painter.getToolTipText(painterPoint, getInsideRect());
	}// getToolTipText

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		GraphicHints.prepareGraphics(g2d);
		
		/* Limiter le dessin à la partie intérieure à la bordure.
		 * On ajoute un clip pour éviter même que "le crayon déborde" sur la
		 * bordure.
		 */
		Rectangle inside = getInsideRect();
		g2d.clipRect(inside.x, inside.y, inside.width, inside.height);
		
		painter.paintDiagram(g2d, inside);
	}// paintComponent
	
	/** Renvoie un rectangle du contenu du composant, hors bordures.
	 * 
	 * @return	Le rectangle intérieur à la bordure, dans les coordonnées du
	 * 			composant lui-même.
	 */
	private Rectangle getInsideRect() {
		Insets insets = getInsets();
		Rectangle bounds = getBounds();
		return new Rectangle(
				insets.left,
				insets.top,
				bounds.width - insets.left - insets.right,
				bounds.height - insets.top - insets.bottom);
	}// getInsideRect

	/** Renvoie le dessinateur du diagramme. */
	public Painter getPainter() {
		return painter;
	}// getPainter
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 100);
	}// getPreferredSize
}
