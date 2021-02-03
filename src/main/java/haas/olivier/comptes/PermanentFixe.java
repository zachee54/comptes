/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

/**
 * L'état d'une opération permanente dont les montants sont fixés à l'avance.
 * 
 * @author Olivier HAAS
 */
@Entity
public class PermanentFixe extends PermanentState {
	private static final long serialVersionUID = -3256470081643511525L;

	/**
	 * Montants prédéfinis par mois.
	 */
	@ElementCollection
	private Map<YearMonth, BigDecimal> montants;

	protected PermanentFixe() {
	}
	
	/**
	 * Construit un état d'opération permanente dont les montants sont fixés à
	 * l'avance.
	 * 
	 * @param montants	Le planning des montants des écritures à générer, selon
	 * 					les mois.
	 */
	public PermanentFixe(Map<Month, BigDecimal> montants) {
		this.montants = Permanent.convertMonths(montants);
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
		YearMonth maxMonthAmount = Permanent.getFloor(montants, month);

		// Si pas de date antérieure définie, lever une exception
		if (maxMonthAmount == null) {
			throw new InconsistentArgumentsException(
					"Pas de montant trouvé avant le mois spécifié");
		}

		// Purger la liste des mois
		Permanent.purgeMapBefore(montants, maxMonthAmount);
		
		// Montant à retenir
		return montants.get(maxMonthAmount);
	}
	
	/**
	 * Renvoie le planning des montants en utilisant une classe personnalisée
	 * pour l'implémentation des mois.
	 * 
	 * @return	Une Map des montants, les clés étant des
	 * 			<code>haas.olivier.util.Month</code>.
	 */
	public Map<Month, BigDecimal> getMontantsByMonth() {
		return Permanent.wrapWithMonth(montants);
	}
}
