/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

/**
 * L'état d'une opération permanente dont les montants sont fixés à l'avance.
 * 
 * @author Olivier HAAS
 */
public class PermanentFixe implements PermanentState {

	/**
	 * Montants prédéfinis par mois.
	 */
	public final Map<Month, BigDecimal> montants;

	/**
	 * Construit un état d'opération permanente dont les montants sont fixés à
	 * l'avance.
	 * 
	 * @param montants	Le planning des montants des écritures à générer, selon
	 * 					les mois.
	 */
	public PermanentFixe(Map<Month, BigDecimal> montants) {
		this.montants = montants;
	}

	/**
	 * Renvoie le montant prédéfini pour le mois spécifié.<br>
	 * S'il n'y a aucun montant prédéfini pour ce mois, la méthode renvoie le
	 * montant prédéfini pour le mois le plus proche avant le mois spécifié.
	 * 
	 * @return	Le montant prédéfini pour ce mois ou pour un mois antérieur.
	 * 
	 * @throws InconsistentArgumentsException 
	 * 			Si aucun montant n'a été spécifié, ni au titre du mois concerné,
	 * 			ni au titre des mois antérieurs.
	 */
	@Override
	public BigDecimal getMontant(Month month)
			throws InconsistentArgumentsException {

		// Parcourir les mois à la recherche du dernier montant antérieur
		Month maxMonthAmount = null;		// Mois à retenir pour les montants
		for (Month m : montants.keySet()) {
			if (!month.before(m)) {			// Mois antérieur à la cible
				if (maxMonthAmount == null) {
					// Rien de défini: prendre cette valeur par défaut
					maxMonthAmount = m;

				} else if (maxMonthAmount.before(m)) {
					// m est plus récent que le max actuel

					maxMonthAmount = m;		// m est le nouveau max

				}
			}
		}

		// Si pas de date antérieure définie, lever une exception
		if (maxMonthAmount == null) {
			throw new InconsistentArgumentsException(
					"Pas de montant trouvé avant le mois spécifié");
		}

		// Purger la liste des mois
		Month today = Month.getInstance();
		for (Iterator<Month> it2 = montants.keySet().iterator();
				it2.hasNext();) {

			// Ce mois est-il obsolète de plus de 12 mois ?
			Month m2 = it2.next().getTranslated(12);
			if (m2.before(maxMonthAmount) && m2.before(today)) {
				it2.remove();
			}
		}
		
		// Montant à retenir
		return montants.get(maxMonthAmount);
	}
}
