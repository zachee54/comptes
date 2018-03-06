/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Un décorateur de <code>TableCellRenderer</code> qui définit des couleurs de
 * fond alternées sur 4 lignes.
 * <p>
 * Les lignes paires utilisent deux couleurs qui alternent. Les lignes impaires
 * sont toutes dessinées dans une couleur intermédiaire. Par exemple si les
 * lignes paires sont noires et blanches, les lignes impaires seront grises.
 *
 * @author Olivier HAAS
 */
public class ColouredRendererDecorator implements TableCellRenderer {

	/**
	 * Le <code>TableCellrenderer</code> délégué.
	 */
	private final TableCellRenderer delegate;

	/**
	 * La première couleur de lignes, utilisée sur les lignes paires en
	 * alternance avec {@link #color2}.
	 */
	private final Color color1;
	
	/**
	 * La deuxième couleur de lignes, utilisée sur les lignes paires en
	 * alternance avec {@link #color1}.
	 */
	private final Color color2;
	
	/**
	 * La couleur intermédiaire, utilisée sur les lignes impaires. Elle fait le
	 * lien entre les couleurs vives <code>color1</code> et <code>color2</code>.
	 */
	private final Color colorInt;
	
	/**
	 * Construit un décorateur de <code>TableCellRenderer</code> qui définit des
	 * couleurs de fond alternées sur 4 lignes.
	 * 
	 * @param delegate	Le <code>TableCellRenderer</code> délégué.
	 * @param color1	Une couleur de ligne.
	 * @param color2	Une autre couleur de ligne.
	 */
	public ColouredRendererDecorator(TableCellRenderer delegate, Color color1,
			Color color2) {
		this.delegate = delegate;
		this.color1 = color1;
		this.color2 = color2;
		
		/* Définir la couleur intermédiaire */
		colorInt = new Color(
				(color1.getRed() + color2.getRed())/2,
				(color1.getGreen() + color2.getGreen())/2,
				(color1.getBlue() + color2.getBlue())/2);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		/*
		 * Mémoriser la couleur de police "normale" en cas de sélection.
		 * 
		 * Certains TableCellRenderers modifient la couleur de police quand la
		 * cellule est sélectionnée. En fonction de la couleur d'arrière-plan,
		 * cela peut rendre le contenu difficilement lisible.
		 * 
		 * Cette opération est effectuée ici car si on le fait après l'appel
		 * principal à getTableCellRendererComponent(...), on risque de modifier
		 * les réglages du TableCellRenderer délégué, puisque certains renderers
		 * font les réglages sur eux-mêmes et renvoient "return this".
		 * 
		 * En pratique avec un DefaultTableCellRenderer ça ne change rien parce
		 * qu'en cas de sélection il ne change que le fond (redéfini ici) et la
		 * police (ce qu'on veut imposer) ; la bordure pointillée dépend de
		 * hasFocus. Mais pour d'autres TableCellRenderers...
		 */
		Color fg = null;
		if (isSelected) {
			fg = delegate.getTableCellRendererComponent(
					table, value, false, hasFocus, row, column).getForeground();
		}
		
		/* Composant du délégué */
		Component comp = delegate.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		
		/* Forcer la couleur de police si ça a été déterminé avant */
		if (fg != null)
			comp.setForeground(fg);

		/* Modifier la couleur de fond */
		if (row % 2 == 1) {						// Lignes impaires
			comp.setBackground(colorInt);
		} else if ((row/2) % 2 == 1) {			// Lignes 2 modulo 4
			comp.setBackground(color2);
		} else {								// Multiples de 4
			comp.setBackground(color1);
		}
		
		return comp;
	}
}