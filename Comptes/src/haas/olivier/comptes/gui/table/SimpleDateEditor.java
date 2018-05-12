package haas.olivier.comptes.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;


/**
 * Un <code>TableCellEditor</code> de base pour les dates.
 * <p>
 * Cette classe utilise un <code>DefaultCellEditor(JTextField)</code> en
 * modifiant seulement l'apparence de la bordure, pour éviter des problèmes
 * d'affichage avec certains L&F (Nimbus en particulier).<br>
 * Elle utilise en plus un format personnalisé pour l'affichage des dates.  
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class SimpleDateEditor extends DefaultCellEditor {
	
	/**
	 * Le format de date.
	 */
	private DateFormat formatter;
	
	/**
	 * Le constructeur lance un <code>DefaultCellEditor</code> avec un
	 * <code>JTextField</code>.
	 */
	public SimpleDateEditor(DateFormat formatter, Color color) {
		super(new JTextField());
		this.formatter = formatter;
		
		// Modifier la bordure. C'est la commande essentielle de la classe !
		editorComponent.setBorder(BorderFactory.createLineBorder(color));
	}
	
	@Override
	public Date getCellEditorValue() {
		try {
			return formatter.parse((String) super.getCellEditorValue());
		} catch (ParseException e) {
			return null;
		}
	}
	
	@Override
	public Component getTableCellEditorComponent(
			JTable table,  Object value,
			boolean isSelected, int row, int column) {
		
		// Récupérer le composant par défaut
		JTextField field =
				(JTextField) super.getTableCellEditorComponent(
						table, value, isSelected, row, column);
		
		// Changer le texte pour l'afficher au bon format
		if (value != null && value instanceof Date) {
			field.setText(formatter.format((Date) value));
		}
		return field;
	}
}
