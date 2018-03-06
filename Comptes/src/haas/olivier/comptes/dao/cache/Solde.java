package haas.olivier.comptes.dao.cache;

import java.math.BigDecimal;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;

/**
 * Le solde d'un compte donné pour un mois donné.
 *
 * @author Olivier Haas
 */
public class Solde {

	/**
	 * Le mois.
	 */
	public final Month month;

	/**
	 * Le compte.
	 */
	public final Compte compte;

	/**
	 * Le solde.
	 */
	public final BigDecimal montant;

	/**
	 * Construit un suivi pour un compte et un mois.
	 * 
	 * @param month		Le mois.
	 * @param compte	Le compte.
	 * @param montant	Le montant.
	 */
	public Solde(Month month, Compte compte, BigDecimal montant) {
		this.month = month;
		this.compte = compte;
		this.montant = montant;
	}
}