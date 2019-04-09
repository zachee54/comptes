/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import java.text.DateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/** Un rendu de dates dans les cellules d'une table.
 * <p>
 * Le format de date peut être personnalisé au moment de l'instanciation.
 * <p>
 * Si la valeur à afficher n'est pas une date, alors c'est le résultat de la
 * méthode <code>toString</code> qui est affiché.
 * 
 * @author Olivier HAAS
 */
public class DateCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -8786944297551204642L;

	/** Le format de date */
	private DateFormat formatter;
	
	/** Construit un rendu de dates personnalisé. */
	public DateCellRenderer(DateFormat formatter) {
		if (formatter == null)
			throw new IllegalArgumentException(
					"Le format de date est obligatoire");
		this.formatter = formatter;
		setHorizontalAlignment(JLabel.CENTER);
	}// constructeur
	
	/** Définit comme texte du composant la date formatée, ou le texte par
	 * défaut de <code>value</code>.
	 * 
	 * @param value	La valeur à afficher, en principe une <code>Date</code>.
	 */
	@Override
	public void setValue(Object value) {
		if (value instanceof Date) {
			setText(formatter.format((Date) value));
		} else if (value != null) {
			setText(value.toString());
		} else {
			setText("");
		}
	}// setValue
}
