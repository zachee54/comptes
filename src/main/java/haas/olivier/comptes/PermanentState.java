package haas.olivier.comptes;

import java.math.BigDecimal;

import haas.olivier.util.Month;

/**
 * L'interface d'état des opérations permanentes. Elle détermine la façon de
 * définir une nouvelle écriture.
 *
 * @author Olivier Haas
 */
public abstract class PermanentState {
	
	/**
	 * Renvoie le montant de l'écriture à générer.
	 * 
	 * @param month	Le mois au titre duquel générer l'écriture.
	 * 
	 * @throws EcritureMissingArgumentException
	 * 				Si les données sont insuffisantes pour instancier
	 * 				l'écriture.
	 * 
	 * @throws InconsistentArgumentException
	 * 				Si des informations manquent pour définir le montant au
	 * 				titre de ce mois.
	 */
	abstract BigDecimal getMontant(Month month)
			throws EcritureMissingArgumentException,
			InconsistentArgumentsException;
}
