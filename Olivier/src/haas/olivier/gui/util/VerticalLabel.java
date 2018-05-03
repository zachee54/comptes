/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;

import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Une étiquette qui affiche son texte verticalement.
 * 
 * @author Olivier HAAS
 */
public class VerticalLabel extends JLabel {
	
	private static final long serialVersionUID = -4935832511665708862L;

	/**
	 * Un décorateur de bordure qui simule une rotation de 90° dans le sens
	 * anti-horaire pendant que le composant est dessiné.
	 * <p>
	 * Date: 18 avr. 2018
	 * @author Olivier HAAS
	 */
	private final class RotatedBorder implements Border {
		
		/**
		 * La bordure à dessiner.
		 */
		private final Border border;

		/**
		 * Construit un décorateur de bordure qui simule une rotation
		 * anti-horaire pendant que le composant est dessiné.
		 *
		 * @param border	La bordure à dessiner.
		 */
		private RotatedBorder(Border border) {
			this.border = border;
		}

		/**
		 * Renvoie les <code>Insets</code> de la bordure, sauf si l'étiquette
		 * est en train d'être dessinée, auquel cas la méthode renvoie des
		 * <code>Insets</code> après une rotation dans le sens anti-horaire.
		 */
		@Override
		public Insets getBorderInsets(Component c) {
			Insets insets = border.getBorderInsets(c);
			if (painting) {
				int tmp = insets.bottom;
				insets.bottom = insets.left;
				insets.left = insets.top;
				insets.top = insets.right;
				insets.right = tmp;
			}
			return insets;
		}

		@Override
		public boolean isBorderOpaque() {
			return border.isBorderOpaque();
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			border.paintBorder(c, g, y, x, width, height);
		}
	}

	/**
	 * Indique si la classe mère est en train de peindre le composant.
	 * Si c'est le cas, alors les méthodes donnant les dimensions du composant
	 * doivent se comporter comme si l'étiquette était horizontale, pour que la
	 * classe mère puisse donner les instructions correctes de dessin.
	 * Le reste du temps, ces méthodes renverront les "vraies" dimensions du
	 * composant, c'est-à-dire avec son orientation verticale.
	 */
	private boolean painting = false;
	
	/**
	 * Construit une étiquette qui s'affiche verticalement.
	 */
	public VerticalLabel() {
		/* Constructeur sans argument, alternative à this(JLabel) */
	}
	
	/**
	 * Construire une étiquette qui s'affiche verticalement.
	 * 
	 * @param text	Le texte à afficher.
	 */
	public VerticalLabel(String text) {
		super(text);
	}
	
	/**
	 * Définit les dimensions préférées de l'étiquette dans le sens vertical
	 * (comme la classe mère, mais en transposant la hauteur et la largeur).
	 */
	@Override
	public void setPreferredSize(Dimension size) {
		super.setPreferredSize(new Dimension(size.height, size.width));
	}
	
	/**
	 * Renvoie les dimensions préférées de l'étiquette en position verticale.
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		return new Dimension(dim.height, dim.width);
	}
	
	/**
	 * Renvoie les insets du composant ou, si la classe mère est en train de
	 * peindre le composant, une rotation des insets pour simuler une
	 * orientation horizontale.
	 * 
	 * @param insets	Les insets "normaux", ceux renvoyés par la classe mère.
	 * @return			Les insets à utiliser pour ce composant.
	 */
	private Insets getVerticalInsets(Insets insets) {
		if (painting) {
			int tmp = insets.bottom;
			insets.bottom = insets.right;
			insets.right = insets.top;
			insets.top = insets.left;
			insets.left = tmp;
		}
		return insets;
	}
	
	/**
	 * Renvoie les insets du composant ou, si la classe mère est en train de
	 * peindre le composant, une rotation des insets pour simuler une
	 * orientation horizontale.
	 * 
	 * @return			Les insets à utiliser pour ce composant.
	 */
	@Override
	public Insets getInsets() {
		return getVerticalInsets(super.getInsets());
	}
	
	/**
	 * Renvoie les insets du composant ou, si la classe mère est en train de
	 * peindre le composant, une rotation des insets pour simuler une
	 * orientation horizontale.
	 * 
	 * @param insets	Un objet <code>Insets</code> qui peut être réutilisé.
	 * @return			Les insets à utiliser pour ce composant.
	 */
	@Override
	public Insets getInsets(Insets insets) {
		return getVerticalInsets(super.getInsets(insets));
	}
	
	@Override
	public int getWidth() {
		return painting ? super.getHeight() : super.getWidth();
	}
	
	@Override
	public int getHeight() {
		return painting ? super.getWidth() : super.getHeight();
	}
	
	@Override
	public void paint(Graphics g) {
		
		/* Faire une rotation-translation avant de dessiner l'étiquette */
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform normal = g2.getTransform();
		g2.transform(								// Rotation 90° gauche
				AffineTransform.getQuadrantRotateInstance(-1));
		g2.translate(-getHeight(),0);				// Translation
		painting = true;							// Tourner les dimensions
		super.paint(g2);							// Dessiner le texte
		painting = false;							// Dimensions normales
		g2.setTransform(normal);					// Rétablir trans par défaut
	}
	
	/**
	 * Définit la nouvelle bordure.
	 * <p>
	 * Cette méthode permet de wrapper la nouvelle bordure pour qu'elle puisse
	 * se dessiner verticalement.
	 * 
	 * @param border	La nouvelle bordure.
	 */
	@Override
	public void setBorder(final Border border) {
		super.setBorder(new RotatedBorder(border));
	}
}