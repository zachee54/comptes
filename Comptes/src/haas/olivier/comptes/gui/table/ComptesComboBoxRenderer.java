package haas.olivier.comptes.gui.table;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.util.Month;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;


/**
 * Un Renderer pour les JComboBox qui affichent des comptes.
 * <p>
 * Les comptes sont listés dans l'ordre naturel. Cette classe ajoute des
 * séparateurs entre les comptes de types différents, et le code couleur de
 * FinancialTable pour les comptes budgétaires de recette ou de dépense.
 * 
 * @author Olivier HAAS
 */
public class ComptesComboBoxRenderer implements ListCellRenderer<Compte> {
	
	/**
	 * Le format monétaire à utiliser.
	 */
	private static final DecimalFormat DF = new DecimalFormat(
			"#,##0.00 \u00A4;- #", new DecimalFormatSymbols(Locale.FRANCE));
	
	/**
	 * Le Renderer délégué.
	 */
	private ListCellRenderer<Object> delegate = new DefaultListCellRenderer();
	
	/**
	 * Un panel englobant le Renderer.
	 */
	private JPanel panel = new JPanel(new BorderLayout());
	
	/**
	 * Un séparateur de menus, utilisé pour séparer les comptes de types
	 * différents.
	 */
	private JSeparator sep = new JSeparator();
	
	/**
	 * Le mois actuel.
	 */
	private Month month = Month.getInstance();
	
	@Override
	public Component getListCellRendererComponent(
			JList<? extends Compte> list, Compte value, int index,
			boolean isSelected, boolean cellHasFocus) {

		// Partir du Component de la classe mère
		Component component = delegate.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		
		// Tool tip avec le nom du compte et son solde
		if (value != null) {
			panel.setToolTipText(
					"<html>"
					+ value + "<br/>"						// Nom
					+ DF.format(value.getHistorique(month))	// Montant
					+ "</html>");
		}
		
		// index==-1 signifie l'item sélectionné
		if (index < 0) {
			return component;	// Renvoyer le composant par défaut
		}

		@SuppressWarnings("unchecked")
		ListModel<Compte> model =
				((JList<Compte>) list).getModel();		// Le modèle

		// Mettre de la couleur en fonction du type dépenses/recettes
		TypeCompte type = model.getElementAt(index).getType();
		if (type == TypeCompte.DEPENSES					// Dépenses
				|| type == TypeCompte.DEPENSES_EN_EPARGNE) {
			component.setForeground(FinancialTable.DEPENSE);
			
		} else if (type == TypeCompte.RECETTES			// Recettes
				|| type == TypeCompte.RECETTES_EN_EPARGNE) {
			component.setForeground(FinancialTable.RECETTE);
		}
		
		// Envelopper d'un JPanel pour un séparateur éventuel
		panel.add(component);							// Ajouter composant
		
		// Pas le 1er item, et type différent du précédent ?
		if (index > 0 && type != model.getElementAt(index - 1).getType()) {
			panel.add(sep, BorderLayout.NORTH);			// Séparateur
		} else {
			panel.remove(sep);							// Pas de séparateur
		}
		return panel;
	}
}
