package haas.olivier.comptes;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;

/** L'état d'un compte budgétaire.
 * <p>
 * Les comptes budgétaires enregistrent les mouvements sur un poste de dépenses
 * ou de recettes.<br>
 * Ils maintiennent à jour un historique des soldes sur 12 mois glissants.
 * 
 * @author Olivier HAAS
 */
class CompteBudgetState implements CompteState {

	/** Le type de compte. */
	private TypeCompte type;
	
	/** Construit un état de compte budgétaire.
	 * 
	 * @param type	Le type de compte.
	 */
	public CompteBudgetState(TypeCompte type) {
		this.type = type;
	}// constructeur
	
	@Override
	public TypeCompte getType() {
		return type;
	}
	
	@Override
	public Long getNumero() {
		return null;
	}
	
	/** Aucune implémentation. */
	@Override
	public void setNumero(Long numero) {
	}

	@Override
	public BigDecimal getSuivi(Compte compte, SuiviDAO suivi, Month month) {
		BigDecimal solde = suivi.get(compte, month);
		return (solde == null) ? BigDecimal.ZERO : solde;
	}

	/**
	 * Modifie le solde du compte en sens inverse de <code>delta</code>, puisque
	 * les comptes budgétaires présentent des soldes en miroir.
	 */
	@Override
	public void addHistorique(Compte compte, Month month, BigDecimal delta)
			throws IOException {
		SuiviDAO historique = DAOFactory.getFactory().getHistoriqueDAO();
		BigDecimal newSolde = historique.get(compte, month).subtract(delta);
		historique.set(compte, month, newSolde);
	}
	
	/**
	 * Aucune implémentation : les comptes budgétaires ne sont pas concernés par
	 * les pointages.
	 */
	@Override
	public void addPointage(Compte compte, Month month, BigDecimal delta) {
	}

	/**
	 * Renvoie une vision inversée par rapport aux comptes bancaires.
	 */
	@Override
	public int getViewSign(Compte compte, Compte debit, Compte credit) {
		if (compte == credit)
			return -1; 
		if (compte == debit)
			return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof CompteBudgetState
				&& type == ((CompteBudgetState) obj).type;
	}
}
