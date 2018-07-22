package haas.olivier.comptes.gui.table;

import haas.olivier.util.Month;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.actions.SoldesObservable;
import haas.olivier.comptes.gui.actions.SoldesObserver;

import java.math.BigDecimal;

/**
 * Un <code>TableModel</code> pour lister les données de suivi d'un compte sur
 * une période donnée.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class SuiviTableModel extends FinancialTableModel
implements SoldesObserver {

	/**
	 * Disposition des colonnes pour un compte bancaire.
	 */
	private static ColumnType[] dispositionBancaire =
		{ColumnType.MOIS, ColumnType.HISTORIQUE, ColumnType.AVUE };

	/**
	 * Disposition des colonnes pour un compte budgétaire.
	 */
	private static ColumnType[] dispositionBudget =
		{ColumnType.MOIS, ColumnType.HISTORIQUE, ColumnType.MOYENNE };

	/**
	 * Construit un modèle de table de suivi d'un compte.
	 */
	public SuiviTableModel(MonthObservable monthObservable,
			CompteObservable compteObservable,
			SoldesObservable soldesObservable) {
		super(monthObservable, compteObservable);
		
		// S'enregistrer auprès de l'observable de soldes
		soldesObservable.addObserver(this);
		
		defineDisposition();	// Définir la disposition de départ
	}
	
	/**
	 * Redéfinit la disposition.
	 * <p>
	 * Cette méthode est utile à l'initialisation du modèle, ou quand la
	 * disposition est modifiée.
	 */
	private void defineDisposition() {
		disposition = (compte == null || compte.getType().isBancaire())
				? dispositionBancaire : dispositionBudget;
	}

	@Override
	public void update() {

		// Récupérer la disposition statique au cas où elle ait changé. 
		defineDisposition();
		
		// Recharger les données
		fireTableDataChanged();
	}
	
	/**
	 * Détermine le nombre de mois à afficher.
	 * 
	 * @return	36
	 */
	@Override
	public int getRowCount() {
		// Afficher les comptes sur 3 ans (36 mois)
		return 36;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Month month = MonthObservable.getMonth().getTranslated(-row);
		switch (disposition[col]) {
		case MOIS:
			return month;
		case HISTORIQUE:
			return compte.getHistorique(month);
		case AVUE:
			return compte.getSoldeAVue(month);
		case MOYENNE:
			return compte.getMoyenne(month);
		default:
			return null;
		}
	}

	@Override
	public void soldesChanged() {
		update();					// Mettre à jour quand les soldes changent
	}
	
	@Override
	public BigDecimal getMontantAt(int row) {
		// Renvoyer par convention le solde théorique du row-ième mois précédent
		return compte.getHistorique(
				MonthObservable.getMonth().getTranslated(-row));
	}
}