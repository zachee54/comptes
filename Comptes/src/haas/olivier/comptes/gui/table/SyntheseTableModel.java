package haas.olivier.comptes.gui.table;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.util.Month;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.FilterCompte;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.actions.SoldesObservable;
import haas.olivier.comptes.gui.actions.SoldesObserver;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un <code>TableModel</code> donnant une vision synthétique des comptes d'un
 * même type à une date donnée.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class SyntheseTableModel extends FinancialTableModel
implements SoldesObserver {

	/**
	 * Disposition des colonnes pour des comptes bancaires.
	 */
	private static ColumnType[] dispositionBancaire =
		{ColumnType.COMPTE, ColumnType.HISTORIQUE, ColumnType.AVUE};
	
	/**
	 * Disposition des colonnes pour des comptes budgétaires.
	 */
	private static ColumnType[] dispositionBudget =
		{ColumnType.COMPTE, ColumnType.HISTORIQUE, ColumnType.MOYENNE};
	
	/**
	 * Le filtre de comptes à utiliser pour savoir lesquels afficher.
	 */
	private FilterCompte filter;
	
	/**
	 * Liste des comptes.
	 */
	private List<Compte> comptes = new ArrayList<Compte>();
	
	/**
	 * Totaux par type de solde.
	 */
	private BigDecimal totalHistorique = BigDecimal.ZERO,
			totalSoldeAVue = BigDecimal.ZERO,
			totalMoyenne = BigDecimal.ZERO;
	
	/**
	 * Construit un modèle de table de synthèse des comptes.
	 * 
	 * @param monthObservable	L'observable de mois.
	 * @param compteObservable	L'observable de compte.
	 * @param soldesObservable	L'observable des soldes.
	 * @param filter			Le filtre déterminant les comptes à afficher.
	 */
	public SyntheseTableModel(
			MonthObservable monthObservable,
			CompteObservable compteObservable,
			SoldesObservable soldesObservable,
			FilterCompte filter) {
		super(monthObservable, compteObservable);
		this.filter = filter;
		
		// S'enregistrer auprès de l'observable de soldes
		soldesObservable.addObserver(this);
		
		// Définir la disposition de départ
		defineDisposition();
	}
	
	/**
	 * Redéfinit la disposition.
	 * <p>
	 * Cette méthode est utile à l'initialisation du modèle, ou quand la
	 * disposition est modifiée.
	 */
	private void defineDisposition() {
		
		// On distingue selon qu'il s'agit d'un compte bancaire ou budgétaire.
		disposition = filter.acceptsBancaires()
				? dispositionBancaire : dispositionBudget;
	}

	@Override
	public int getRowCount() {
		return comptes.size() + 1;			// Les comptes + la ligne de total
	}

	@Override
	public void update() {
		
		// Récupérer la disposition statique au cas où elle ait changé. 
		defineDisposition();
		
		// Remettre à zéro les totaux
		totalHistorique = totalSoldeAVue = totalMoyenne = BigDecimal.ZERO;

		Month month = MonthObservable.getMonth();			// Mois à utiliser
		
		// Récupérer les comptes
		comptes = new ArrayList<Compte>();
		try {
			// Parcourir tous les comptes
			for (Compte c :
				DAOFactory.getFactory().getCompteDAO().getAll()) {
				
				// Si le type du compte correspond
				if (filter.accepts(c)) {
					
					// Ajouter le compte
					comptes.add(c);
				
					// Ajouter les soldes pour calculer les totaux
					totalHistorique =					// Solde théorique
							totalHistorique.add(c.getHistorique(month));
					totalSoldeAVue =					// Solde à vue
							totalSoldeAVue.add(c.getSoldeAVue(month));
					if (c instanceof CompteBudget) {	// Si compte budgétaire
						totalMoyenne = totalMoyenne.add(// Moyenne
								((CompteBudget) c).getMoyenne(month));
					}
				}
			}
			
			// Trier la liste de comptes
			Collections.sort(comptes);

			// Recharger les données
			fireTableDataChanged();

		} catch (IOException e) {
			// TODO Exception à traiter
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
			
		// Selon que c'est un total (dernière ligne) ou un compte
		if (row < comptes.size()) {							// Ligne de compte
			Compte c = comptes.get(row);					// Compte à utiliser
			Month month = MonthObservable.getMonth();		// Mois à utiliser
			
			switch (disposition[col]) {						// Selon la colonne
			case COMPTE :	return c;						// Le compte
			case HISTORIQUE:return c.getHistorique(month);	// Solde théorique
			case AVUE :		return c.getSoldeAVue(month);	// Solde à vue
			case MOYENNE :
				if (c instanceof CompteBudget)				// Si compte budget
					return ((CompteBudget) c).getMoyenne(month);// Moyenne
				// Pas de break : si compte bancaire, pas de moyenne (null)
			default:
				return null;
			}
			
		} else {											// Ligne de total
			switch (disposition[col]) {						// Selon la colonne
			case COMPTE:	return "Total";					// Titre des totaux
			case HISTORIQUE:return totalHistorique;			// Total théorique
			case AVUE:		return totalSoldeAVue;			// Total à vue
			case MOYENNE:	return totalMoyenne;			// Total moyenne
			default:		return null;
			}
		}
	}
	
	/**
	 * Renvoie par convention le solde à vue du compte pour le mois en cours.
	 */
	@Override
	public BigDecimal getMontantAt(int row) {
		if (row < comptes.size()) {	// La ligne est dans la liste des commptes
			return comptes.get(row)	// Renvoyer le solde du compte à ce mois
					.getSoldeAVue(MonthObservable.getMonth());
		} else {					// Ligne de total
			return totalSoldeAVue;	// Renvoyer le total des soldes à vue
		}
	}
	
	/**
	 * Renvoie l'index de la ligne contenant le compte actuellement sélectionné.
	 */
	public int getActualCompteRow() {
		return comptes.indexOf(compte);	// C'est l'index du compte dans la liste
	}

	@Override
	public void soldesChanged() {
		update();					// Mettre à jour quand les soldes changent
	}
}
