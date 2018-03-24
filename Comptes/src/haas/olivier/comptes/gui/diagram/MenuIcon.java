package haas.olivier.comptes.gui.diagram;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

/**
 * Une icône de taile prédéfinie symbolisant un menu
 * 
 * @author Olivier Haas
 */
class MenuIcon implements Icon {
	
	/**
	 * La hauteur de l'icône.
	 */
	private final int height;
	
	/**
	 * Construit une icône symbolisant un menu.
	 * 
	 * @param size	La Hauteur de l'icône.
	 */
	MenuIcon(int height) {
		this.height = height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		
		// Réglages graphiques
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,			// Antialiasing
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(
				((float) height)/8,							// Épaisseur (float)
				BasicStroke.CAP_ROUND,						// Bouts ronds
				BasicStroke.JOIN_ROUND));
		
		/*
		 * Épaisseur des traits
		 * Les traits sont disposés au quart de la hauteur, à la moitié et aux
		 * trois quarts. L'épaisseur des traits est la moitié des écarts afin de
		 * laisser une largeur équivalente à la couleur de fond ; donc 1/8.
		 */
		int thickness = height/6;
		
		// Tracer les traits
		int h;												// Hauteur du trait
		for (int i=0; i<3; i++) {
			h = (2*i+1)*height/6;
			g2d.drawLine(x+thickness, y+h, x+getIconWidth()-thickness, y+h);
		}
	}

	@Override
	public int getIconWidth() {
		return height*4/3;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
}