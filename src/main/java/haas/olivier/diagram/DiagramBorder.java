/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

class DiagramBorder implements Border {
	
	/** La taille de chaque bordure. */
	private static final Insets INSETS = new Insets(0, 0, 3, 0);
	
	/** Crée une bordure pour un diagramme.
	 * 
	 * @param model		Le modèle du diagramme.
	 * @param intervals	<code>true</code> si les abscisses correspondent à des
	 * 					intervalles entre deux graduations, <code>false</code>
	 * 					si les abscisses correspondent aux graduations.
	 */
	public static Border newDiagramBorder(DiagramModel model,
			boolean intervals) {
		return new DiagramBorder(model, intervals);
	}// newDiagramBorder
	
	/** Le modèle du diagramme. */
	private final DiagramModel model;
	
	/** <code>true</code> si les abscisses correspondent à des intervalles entre
	 * deux graduations, <code>false</code> si les abscisses correspondent aux
	 * graduations.
	 */
	private final boolean intervals;
	
	/** Construit une bordure pour un diagramme.
	 * 
	 * @param model		Le modèle du diagramme.
	 * @param intervals	<code>true</code> si les abscisses correspondent à des
	 * 					intervalles entre deux graduations, <code>false</code>
	 * 					si les abscisses correspondent aux graduations.
	 */
	private DiagramBorder(DiagramModel model, boolean intervals) {
		this.model = model;
		this.intervals = intervals;
	}// constructeur
	
	/** Trace des graduations verticales en face de chaque abscisse. */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		
		// Coordonnées intérieures de la bordure
		int left = x + INSETS.left;
		int right = x + width - 1 - INSETS.right;	// Dernier pixel visible
		int bottom = y + height - 1;
		int top = bottom - INSETS.bottom;	// Le haut de la bordure du bas !!
		
		// Nunméro de la dernière graduation à tracer
		int count = model.getXValues().length;
		int last = intervals ? count : count - 1;
		
		// Tracer les graduations du bas
		for (int i=0; i<=last; i++) {
			int abscisse = left + (right - left) * i / last;
			g.drawLine(abscisse, top, abscisse, bottom);
		}
	}// paintBorder

	@Override
	public Insets getBorderInsets(Component c) {
		return INSETS;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

}
