/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;

/** Un échantillon de dessin de d'une série.
 * <p>
 * Cette classe sert à afficher un exemple de tracé de chaque série dans la
 * légende.
 * 
 * @author Olivier HAAS
 */
class SerieSample implements Icon {
	
	/** La série à tracer. */
	private final Serie serie;

	/** Le dessinateur de diagramme qui dessinera l'échantillon. */
	private final Painter painter;
	
	/** Construit un échantillon du tracé d'une série.
	 * 
	 * @param painter	Le dessinateur 
	 */
	SerieSample(Serie serie, Painter painter) {
		this.serie = serie;
		this.painter = painter;
	}// constructeur
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		painter.paintSample(serie, (Graphics2D) g,
				new Rectangle(x, y, getIconWidth(), getIconHeight()));
	}// paintIcon

	@Override
	public int getIconWidth() {
		return 20;
	}// getIconWidth

	@Override
	public int getIconHeight() {
		return 10;
	}// getIconHeight

}
