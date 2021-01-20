/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

/**
 * L'état d'un compte bancaire.
 * <p>
 * Les comptes bancaires ont la particularité que si aucune opération n'est
 * enregistrée au titre d'un mois, le solde du compte est celui du mois
 * précédent (et non zéro).<br>
 * De plus, les comptes bancaires doivent gérer la distinction entre les soldes
 * théoriques, qui s'appuient sur les montants des opérations passées au cours
 * du mois, et les soldes à vue qui ne sont influencés que par les dates de
 * pointage.
 * 
 * @author Olivier HAAS
 */
@Entity
class CompteBancaireState extends CompteState {
	private static final long serialVersionUID = -225986016944002291L;

	/**
	 * Format de rendu des numéros de compte.
	 * <p>
	 * Cet objet n'est pas synchronisé et ne doit donc pas être accédé de
	 * manière statique.
	 */
	@Transient
	private final NumberFormat format = NumberFormat.getInstance();

	/**
	 * Le type de compte.
	 */
	@Enumerated(EnumType.STRING)
	private TypeCompte type;
	
	/**
	 * Numéro de compte.
	 */
	private Long numero;

	/**
	 * Construit un état de compte bancaire.
	 * 
	 * @param type	Le type de compte.
	 * @param old	L'ancien état du compte. La nouvelle instance reprendra le
	 * 				même numéro de compte, s'il existe.
	 */
	public CompteBancaireState(TypeCompte type, CompteState old) {
		this.type = type;
		this.numero = (old == null) ? null : old.getNumero();
	}
	
	@Override
	public TypeCompte getType() {
		return type;
	}

	@Override
	public Long getNumero() {
		return numero;
	}

	@Override
	public void setNumero(Long numero) {
		this.numero = numero;
	}

	/**
	 * @return	Le solde du mois, à défaut le dernier solde connu avant ce mois,
	 * 			ou zéro s'il n'y a aucun solde à ce mois ni avant.
	 */
	@Override
	public BigDecimal getSuivi(Compte compte, SuiviDAO dao, Month month) {
		Date ouverture = compte.getOuverture();
		if (ouverture == null)
			ouverture = DAOFactory.getFactory().getDebut().getFirstDay();
	
		// Remonter mois par mois jusqu'à la date d'ouverture si besoin
		for(Month m = month; !m.before(ouverture); m = m.getPrevious()) {
			BigDecimal solde = dao.get(compte, m);
			if (solde != null)
				return solde;
		}
	
		// Valeur par défaut
		return BigDecimal.ZERO;
	}

	@Override
	public void addHistorique(Compte compte, Month month, BigDecimal delta) {
		addSuivi(compte, DAOFactory.getFactory().getHistoriqueDAO(), month,
				delta);
	}
	
	@Override
	public void addPointage(Compte compte, Month month, BigDecimal delta) {
		addSuivi(compte, DAOFactory.getFactory().getSoldeAVueDAO(), month,
				delta);
	}
	
	/**
	 * Modifie le suivi du compte au titre d'un mois.
	 * 
	 * @param compte	Le compte.
	 * @param suivi		Le suivi à modifier.
	 * @param month		Le mois au titre duquel modifier le suivi.
	 * @param delta		Le montant à ajouter au suivi du mois.
	 */
	private void addSuivi(Compte compte, SuiviDAO suivi, Month month,
			BigDecimal delta) {
		BigDecimal solde = getSuivi(compte, suivi, month);
		suivi.set(compte, month, solde.add(delta));
	}

	@Override
	public int getViewSign(Compte compte, Compte debit, Compte credit) {
		if (compte == credit)
			return 1; 
		if (compte == debit)
			return -1;
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof CompteBancaireState))
			return false;
		
		CompteBancaireState compteBancaireState = (CompteBancaireState) obj;
		
		// Comparer les types
		if (type != compteBancaireState.type)
			return false;
		
		// Comparer les numéros
		if (numero == null) {
			return compteBancaireState.numero == null;
		} else {
			return numero.equals(compteBancaireState.numero);
		}
	}
	
	/**
	 * Le hash code est celui du {@link #type}.
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}

	/**
	 * @return	Le numéro du compte, précédé d'une espace. Si le numéro de
	 * 			compte est <code>null</code>, renvoie une chaîne vide.
	 */
	@Override
	public synchronized String toString() {
		return numero == null ? "" : " n°" + format.format(numero);
	}
}
