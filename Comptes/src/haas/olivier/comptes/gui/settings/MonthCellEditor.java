package haas.olivier.comptes.gui.settings;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import haas.olivier.util.Month;

/**
 * Un <code>TableCellEditor</code> pour la classe <code>Month</code>.
 */
class MonthCellEditor extends DefaultCellEditor {
	private static final long serialVersionUID = -7339414009962489572L;

	/**
	 * Construit un éditeur avec <code>JComboBox</code>.
	 */
	public MonthCellEditor() {
		super(new JComboBox<Month>());
	}

	/**
	 * Renvoie une <code>JComboBox</code> pré-remplie avec les 12 mois avant et
	 * après le mois de référence. Si le mois de référence est
	 * <code>null</code>, par rapport à la date du jour.
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		// ComboBox générée par la classe mère
		@SuppressWarnings("unchecked")
		JComboBox<Month> comboBox =
				(JComboBox<Month>) super.getTableCellEditorComponent(
						table, value, isSelected, row, column);
		
		Month reference =						// Mois de référence (ou actuel)
				(value instanceof Month) ? (Month) value : new Month();
		
		// Re-remplir, de -12 mois à +12 mois
		comboBox.removeAllItems();
		for (int i=-12; i<=12; i++) {
			comboBox.addItem(reference.getTranslated(i));
		}
		
		// Sélectionner le mois de référence par défaut
		comboBox.setSelectedItem(reference);
		
		return comboBox;
	}
}// class MonthCellEditor