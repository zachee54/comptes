/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * L'état d'un compte budgétaire.
 * <p>
 * Les comptes budgétaires enregistrent les mouvements sur un poste de dépenses
 * ou de recettes.<br>
 * Ils maintiennent à jour un historique des soldes sur 12 mois glissants.
 * 
 * @author Olivier HAAS
 */
@Entity
class CompteBudgetState extends CompteState {
	private static final long serialVersionUID = -1159441918416714602L;

	/**
	 * Le type de compte.
	 */
	@Enumerated(EnumType.STRING)
	private TypeCompte type;
	
	protected CompteBudgetState() {
	}
	
	/**
	 * Construit un état de compte budgétaire.
	 * 
	 * @param type	Le type de compte.
	 */
	public CompteBudgetState(TypeCompte type) {
		this.type = type;
	}
	
	@Override
	public TypeCompte getType() {
		return type;
	}
	
	@Override
	public Long getNumero() {
		return null;
	}
	
	/**
	 * Aucune implémentation.
	 */
	@Override
	public void setNumero(Long numero) {
		// Les comptes budgétaires ne gèrent pas les numéros
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
	public void addHistorique(Compte compte, Month month, BigDecimal delta) {
		SuiviDAO historique = DAOFactory.getFactory().getHistoriqueDAO();
		BigDecimal solde = getSuivi(compte, historique, month);
		historique.set(compte, month, solde.subtract(delta));
	}
	
	/**
	 * Aucune implémentation.
	 */
	@Override
	public void addPointage(Compte compte, Month month, BigDecimal delta) {
		// Les comptes budgétaires ne sont pas concernés par les pointages
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

	/**
	 * Le hash code est celui du {@link #type}.
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
