/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

/**
 * L'interface des différents états d'un compte.
 * <p>
 * Les classes concrètes implémentent par exemple le comportement des comptes
 * bancaires, ou des comptes budgétaires.
 *
 * @author Olivier Haas
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class CompteState implements Serializable {
	private static final long serialVersionUID = 6193837940341269422L;
	
	/**
	 * Identifiant unique.
	 */
	@Id
	@GeneratedValue
	private Integer id;
	
	protected CompteState() {
	}
	
	/**
	 * Renvoie l'identifiant unique.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Renvoie le type de compte.
	 */
	abstract TypeCompte getType();
	
	/**
	 * Renvoie le numéro du compte.
	 * 
	 * @return	Le numéro de compte, ou <code>null</code> si le compte n'a pas
	 * 			de numéro.
	 */
	abstract Long getNumero();
	
	/**
	 * Définit le numéro du compte.
	 * 
	 * @param numero	Le nouveau numéro. Peut être <code>null</code>.
	 */
	abstract void setNumero(Long numero);
	
	/**
	 * Renvoie le solde du compte à la fin du mois spécifié.
	 * 
	 * @param compte	Le compte.
	 * @param suivi		Le suivi souhaité : historique, solde à vue ou moyennes.
	 * @param month		Le mois.
	 * @return			Le solde du compte à la fin du mois, selon
	 * 					<code>suivi</code>.
	 */
	abstract BigDecimal getSuivi(Compte compte, SuiviDAO suivi, Month month);
	
	/**
	 * Modifie l'historique du compte au titre d'un mois, en lui ajoutant le
	 * montant spécifié.
	 * 
	 * @param compte	Le compte dont le solde doit être modifié.
	 * @param month		Le mois au titre duquel modifier le solde.
	 * @param delta		Le montant à ajouter au solde actuel.
	 */
	abstract void addHistorique(Compte compte, Month month, BigDecimal delta);
	
	/**
	 * Modifie le solde à vue du compte au titre d'un mois, en lui ajoutant le
	 * montant spécifié.
	 * 
	 * @param compte	Le compte dont le solde doit être modifié.
	 * @param month		Le mois au titre duquel modifier le solde.
	 * @param delta		Le montant à ajouter au solde actuel.
	 */
	abstract void addPointage(Compte compte, Month month, BigDecimal delta);
	
	/**
	 * Renvoie le sens dans lequel il faut lire l'écriture en consultant le
	 * compte.
	 * <p>
	 * Lorsqu'une écriture apparaît dans le relevé du compte, le montant doit en
	 * principe être reproduit tel quel si le compte figure au crédit, ou pour
	 * son opposé si le compte figure au débit.
	 * <p>
	 * Cette méthode donne la clé de lecture en fonction du compte au débit et
	 * du compte au crédit.
	 * 
	 * @param compte	Le compte concerné.
	 * @param debit		Le compte figurant au débit.
	 * @param credit	Le compte figurant au débit.
	 * 
	 * @return			1 si le montant doit figurer tel quel dans le relevé,
	 * 					-1 si c'est l'opposé du montant qui doit figurer,
	 * 					0 si aucun des comptes ne correspond à l'instance.
	 */
	public abstract int getViewSign(Compte compte, Compte debit, Compte credit);
}
