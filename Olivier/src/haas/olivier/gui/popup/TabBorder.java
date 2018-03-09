/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.popup;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.border.Border;

/** Une bordure curviligne personnalisée pour l'onglet du popup.<br>
 * Elle dessine les bords de l'onglet avec des courbes de Bézier.
 * <p>
 * <b>Attention :</b> cette bordure est en principe réservée à l'usage avec des
 * <code>JPanel</code>, du fait qu'elle remplit l'intérieur de l'onglet dessiné,
 * y compris la zone où se trouve le composant lui-même.<br>
 * Appliquée sur un <code>JLabel</code>, par exemple, elle sera dessinée après
 * le texte de l'étiquette et la "recouvrera" de la couleur de fond, ce qui
 * masquera le texte.<br>
 * Appliquée à un <code>JPanel</code> en revanche, elle est dessinée avant les
 * composants enfants et donne donc l'impression visuelle que les composants
 * sont dessinés sur un fond jaune avec une bordure curviligne.
 * 
 * @author Olivier HAAS
 */
class TabBorder implements Border {

	/** La taille de la bordure. */
	private final Insets insets = new Insets(
			RootPaneWithPopup.BORDER_THICKNESS,
			10,
			RootPaneWithPopup.BORDER_THICKNESS,
			10);
	
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		
		/* Coordonnées des extrêmes visibles (1px avant les bords droit et bas,
		 * auquel se rajoute l'épaisseur du pinceau)
		 */
		int bottom = y + height - RootPaneWithPopup.BORDER_THICKNESS/2 - 1;
		int right = x + width - 1;
		
		// Distance entre les extrémités de Bézier et les points de contrôle
		double ctrl = insets.left*3/4;
		
		// Construire le chemin à tracer
		Path2D path = new Path2D.Double();
		path.moveTo(x, y);
		path.curveTo(
				ctrl, y,
				insets.left-ctrl, bottom,
				insets.left, bottom);
		path.lineTo(width-insets.right, bottom);
		path.curveTo(
				right-insets.right+ctrl, bottom,
				right-ctrl, y,
				right, y);
		
		drawPath((Graphics2D) g, path);
	}// paintBorder
	
	/** Dessine le bord de l'onglet et la couleur de fond de la bordure.
	 * 
	 * @param g		Le contexte graphique.
	 * @param path	Le chemin qui trace le contour de la bordure.
	 */
	private void drawPath(Graphics2D g, Path2D path) {
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(RootPaneWithPopup.BORDER_THICKNESS));
		
		// Remplir l'intérieur avec la couleur du popup
		g.setColor(RootPaneWithPopup.POPUP_BACKGROUND);
		g.fill(path);
		
		// Dessiner le bord
		g.setColor(RootPaneWithPopup.BORDER_COLOR);
		g.draw(path);
	}// drawPath

	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}// getBorderInsets

	@Override
	public boolean isBorderOpaque() {
		return true;
	}// isBorderOpaque
}
