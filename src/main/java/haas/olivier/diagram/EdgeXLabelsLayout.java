/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/** Un agenceur d'étiquettes des abscisses qui place les étiquettes face à
 * chaque graduation.
 * 
 * @author Olivier HAAS
 * @see {@link XLabels}
 */
class EdgeXLabelsLayout implements LayoutManager {

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int height = 0;
		for (Component child : parent.getComponents())
			height = Math.max(height, child.getPreferredSize().height);
		return new Dimension(0, height);
	}// preferredLayoutSize

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		int height = 0;
		for (Component child : parent.getComponents())
			height = Math.max(height, child.getMinimumSize().height);
		return new Dimension(0, height);
	}// minimumLayoutSize

	@Override
	public void layoutContainer(Container parent) {
		int leftOverflow = ((XLabels) parent).getLeftOverflow();
		int rightOverflow = ((XLabels) parent).getRightOverflow();
		int diagramWidth = parent.getWidth() - leftOverflow - rightOverflow;
		
		Component[] children = parent.getComponents();
		Rectangle previousBounds = null;
		
		for (int i=0; i<children.length; i++) {
			Component child = children[i];
			Dimension prefSize = child.getPreferredSize();
			
			// Abscisse de la graduation proprement dite
			int x = leftOverflow + diagramWidth * i / (children.length - 1);
			
			// Rectangle de l'étiquette centré horizontalement sur la graduation
			Rectangle bounds = new Rectangle(
					x - prefSize.width / 2,
					0,
					prefSize.width,
					prefSize.height);
			
			// Si on a la place d'écrire cette étiquette (pas de chevauchement)
			if (previousBounds == null || !previousBounds.intersects(bounds)) {
				child.setBounds(bounds);
				previousBounds = bounds;
				
			} else {
				child.setBounds(0,0,0,0);			// Rectangle de taille nulle
			}// if
		}// for
	}// layoutContainer

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}
}
