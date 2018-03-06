package haas.olivier.comptes.dao;

import java.math.BigDecimal;
import java.util.Iterator;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.cache.Solde;
import haas.olivier.util.Month;

/** L'interface d'accès aux données de suivi.
 * 
 * @author Olivier HAAS
 */
public interface SuiviDAO {

	/**
	 * Renvoie les suivis de tous les comptes pour tous les mois.
	 */
	public Iterator<Solde> getAll();
	
	/**
	 * Renvoie le suivi d'un compte pour un mois donné.
	 * 
	 * @param compte	Le compte.
	 * @param month		Le mois au titre duquel on veut la donnée.
	 * 
	 * @return			Le montant du suivi au titre du mois <code>month</code>
	 * 					du compte, ou <code>null</code> si aucun suivi n'est
	 * 					défini pour ce compte ou pour ce mois.
	 */
	public BigDecimal get(Compte compte, Month month);
	
	/**
	 * Définit ou modifie le suivi d'un compte pour un mois donné.
	 * 
	 * @param solde	Le solde à modifier.
	 */
	public void set(Solde solde);
	
	/**
	 * Définit ou modifie le suivi d'un compte pour un mois donné.
	 * 
	 * @param compte	Le compte.
	 * @param month		Le mois au titre duquel affecter la donnée.
	 * @param montant	Le nouveau montant.
	 */
	public void set(Compte compte, Month month, BigDecimal montant);
	
	/**
	 * Efface les données de suivi du mois spécifié et des mois suivants.
	 * 
	 * @param debut	Le mois à partir duquel supprimer les données de suivi.
	 */
	public void removeFrom(Month debut);

}
