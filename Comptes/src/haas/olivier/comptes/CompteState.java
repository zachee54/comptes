package haas.olivier.comptes;

import java.io.Serializable;
import java.math.BigDecimal;

import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

/** L'interface des différents états d'un compte.
 * <p>
 * Les classes concrètes implémentent par exemple le comportement des comptes
 * bancaires, ou des comptes budgétaires.
 *
 * @author Olivier Haas
 */
interface CompteState extends Serializable {

	/** Renvoie le type de compte */
	TypeCompte getType();
	
	/** Renvoie le numéro du compte.
	 * 
	 * @return	Le numéro de compte, ou <code>null</code> si le compte n'a pas
	 * 			de numéro.
	 */
	Long getNumero();
	
	/** Définit le numéro du compte.
	 * 
	 * @param numero	Le nouveau numéro. Peut être <code>null</code>.
	 */
	void setNumero(Long numero);
	
	/**
	 * Renvoie le solde du compte à la fin du mois spécifié.
	 * 
	 * @param compte	Le compte.
	 * @param suivi		Le suivi souhaité : historique, solde à vue ou moyennes.
	 * @param month		Le mois.
	 * @return			Le solde du compte à la fin du mois, selon
	 * 					<code>suivi</code>.
	 */
	BigDecimal getSuivi(Compte compte, SuiviDAO suivi, Month month);
	
	/**
	 * Modifie l'historique du compte au titre d'un mois, en lui ajoutant le
	 * montant spécifié.
	 * 
	 * @param compte	Le compte dont le solde doit être modifié.
	 * @param month		Le mois au titre duquel modifier le solde.
	 * @param delta		Le montant à ajouter au solde actuel.
	 */
	void addHistorique(Compte compte, Month month, BigDecimal delta);
	
	/**
	 * Modifie le solde à vue du compte au titre d'un mois, en lui ajoutant le
	 * montant spécifié.
	 * 
	 * @param compte	Le compte dont le solde doit être modifié.
	 * @param month		Le mois au titre duquel modifier le solde.
	 * @param delta		Le montant à ajouter au solde actuel.
	 */
	void addPointage(Compte compte, Month month, BigDecimal delta);
	
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
	public int getViewSign(Compte compte, Compte debit, Compte credit);
}
