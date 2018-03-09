/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/** Un Renderer pour les valeurs numériques Number.
 * <p>
 * Il permet d'afficher un séparateur de milliers et d'aligner les nombres à
 * droite.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class NumberCellRenderer extends DefaultTableCellRenderer {

	/** Format numérique. */
	private NumberFormat formatter;
	
	/** Construit un rendu de valeurs numériques de type <code>Number</code>.
	 * 
	 * @param formatter	Le format numérique à utiliser.
	 */
	public NumberCellRenderer(NumberFormat formatter) {
		this.formatter = formatter;
		setHorizontalAlignment(JLabel.RIGHT);
	}// constructeur
	
	@Override
	public void setValue(Object value) {
		if (value instanceof Number) {
			setText(formatter.format(value));
		} else if (value != null) {
			setText(value.toString());
		} else {
			setText("");
		}
	}// setValue
}// class BigDecimalCellRenderer