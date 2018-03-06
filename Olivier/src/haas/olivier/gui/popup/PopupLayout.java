/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.popup;

import static java.lang.Math.min;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

import javax.swing.event.ChangeListener;

/**
 * Un <code>LayoutManager</code> qui dispose un popup et son onglet sur le bord
 * supérieur d'un conteneur en le masquant partiellement donnant l'illusion que
 * le popup peut descendre progressivement en le "tirant" par l'onglet avec la
 * souris.
 *
 * @author Olivier HAAS
 */
class PopupLayout implements LayoutManager2 {

	/**
	 * Le numéro du layer contenant le popup.
	 * 
	 * @see {@link #addLayoutComponent(Component, Object)}.
	 */
	public static final Integer POPUP_LAYER = 0;
	
	/**
	 * Le numéro du layer contenant l'onglet du popup.
	 * 
	 * @see {@link #addLayoutComponent(Component, Object)}.
	 */
	public static final Integer TAB_LAYER = 1;
	
	/**
	 * L'objet qui traite les événements de souris.
	 */
	private final PopupMouseListener mouseListener;
	
	/**
	 * L'objet gérant l'animation visuelle du popup.
	 */
	private final PopupAnimator animator;

	/**
	 * Le composant servant de popup.
	 */
	private Component popup;
	
	/**
	 * Le composant servant d'onglet.
	 */
	private Component tab;
	
	/**
	 * Construit un nouvel agenceur pour un conteneur contenant uniquement un
	 * popup et son onglet.
	 * 
	 * @param changeListener	Le composant auquel notifier les changements de
	 * 							ratio d'affichage. Typiquement, il s'agit du
	 * 							{@link RootPaneWithPopup}.
	 */
	public PopupLayout(ChangeListener changeListener) {
		this.animator = new PopupAnimator(changeListener);
		mouseListener = new PopupMouseListener(animator);
	}

	/**
	 * Sécurise une dimension contre les overflows. Si l'une des dimensions est
	 * négative, alors elle est convertie en <code>Integer.MAX_VALUE</code>.
	 * <p>
	 * Cela arrive par exemple si le composant popup d'origine a une hauteur
	 * préférée égale à <code>Integer.MAX_VALUE</code>, à laquelle s'ajoute
	 * l'épaisseur de la bordure du <code>JPanel</code> dans lequel on l'a
	 * inséré...
	 * 
	 * @param dim	La dimension à sécuriser.
	 * @return		<code>dim</code>, après modification éventuelle.
	 */
	private static Dimension secureSize(Dimension dim) {
		if (dim.width < 0)
			dim.width = Integer.MAX_VALUE;
		if (dim.height < 0)
			dim.height = Integer.MAX_VALUE;
		return dim;
	}

	/**
	 * Renvoie la plus petite dimension supérieure à la fois en largeur et en
	 * hauteur aux deux dimensions spécifiées.
	 * 
	 * @param dim1	Une dimension.
	 * @param dim2	Une dimension.
	 * 
	 * @return		Une dimension dont la largeur est la plus grande des deux
	 * 				largeurs spécifiées, et la hauteur est la plus grande des
	 * 				deux hauteurs spécifiées.
	 */
	private static Dimension getMaxOf(Dimension dim1, Dimension dim2) {
		return new Dimension(
				Math.max(dim1.width, dim2.width),
				Math.max(dim1.height, dim2.height));
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (POPUP_LAYER.equals(constraints)) {
			mouseListener.unregister(popup);
			popup = comp;
		} else if (TAB_LAYER.equals(constraints)) {
			mouseListener.unregister(tab);
			tab = comp;
		} else {
			throw new IllegalArgumentException(
					"Unknown constraint : " + constraints);
		}
		mouseListener.register(comp);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// Non implémenté
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		mouseListener.unregister(comp);
		if (tab == comp)
			tab = null;
		if (popup == comp)
			popup = null;
	}

	@Override
	public void layoutContainer(Container parent) {
		if (popup == null || tab == null)
			return;

		Dimension parentDim = parent.getSize();
		int popupHeight = min(
				secureSize(popup.getPreferredSize()).height,
				parentDim.height + RootPaneWithPopup.BORDER_THICKNESS);
		double ratio = animator.getRatio();
		int popupBottom = (int) (popupHeight * ratio);
		layoutPopup(popupBottom - popupHeight, popupHeight, parentDim);
		layoutTab(popupBottom, parentDim);
	}
	
	/**
	 * Place le popup.
	 * 
	 * @param y			L'ordonnée à laquelle placer le popup.
	 * @param height	La hauteur à donner au popup.
	 * @param parentDim	La dimension du parent.
	 */
	private void layoutPopup(int y, int height, Dimension parentDim) {
		if (y + height <= 0) {
			popup.setBounds(0, 0, 0, 0);
		} else {
			popup.setBounds(0, y, parentDim.width, height);
		}
	}
	
	/**
	 * Place l'onglet.
	 * 
	 * @param popupBottom	L'ordonnée du bas du popup.
	 * @param parentDim		La dimension du parent.
	 */
	private void layoutTab(int popupBottom, Dimension parentDim) {
		if (popupBottom >= parentDim.height) {
			
			/*
			 * Quand l'onglet n'est plus visible parce que le popup remplit tout
			 * l'espace, le fait de neutraliser l'onglet permet d'avoir un réel
			 * événement mouseExited si la souris quitte le popup par l'endroit
			 * où devrait se trouver l'onglet.
			 */
			tab.setBounds(0, 0, 0, 0);
			
		} else {
			Dimension pref = secureSize(tab.getPreferredSize());
			int width = min(pref.width, parentDim.width);
			int height = min(pref.height, parentDim.height);
			tab.setBounds(
					parentDim.width - width,
					popupBottom-RootPaneWithPopup.BORDER_THICKNESS,// Chevauchement
					width,
					height);
		}
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		if (popup == null || tab == null)
			return new Dimension(0, 0);
		
		return getMaxOf(
				secureSize(popup.getPreferredSize()),
				secureSize(tab.getPreferredSize()));
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		if (popup == null || tab == null)
			return new Dimension(0, 0);
		return getMaxOf(
				secureSize(popup.getMinimumSize()),
				secureSize(tab.getMinimumSize()));
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return popup == null
				? new Dimension(Integer.MAX_VALUE, Integer.MIN_VALUE)
				: secureSize(popup.getMaximumSize());
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
		// Rien à faire
	}
}
