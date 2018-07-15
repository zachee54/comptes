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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Solde))
			return false;
		
		Solde solde = (Solde) o;
		return month.equals(solde.month)
				&& compte.equals(solde.compte)
				&& montant.equals(solde.montant);
	}
	
	@Override
	public int hashCode() {
		int h = 73;
		h = h * 59 + month.hashCode();
		h = h * 59 + compte.hashCode();
		h = h * 59 + montant.hashCode();
		return h;
	}
}