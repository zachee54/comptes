package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class PermanentProport extends Permanent {
	private static final long serialVersionUID = 715136648888028015L;

	/** Écriture permanente dont dépend celle-ci. */
	public final Permanent dependance;
	
	/** Coefficient multiplicateur, exprimé en pourcentage. */
	public final BigDecimal taux;

	/** Construit une opération permanente qui génère des écritures dont le
	 * montant est proportionnel à une autre opération permanente
	 * 
	 * @param id			L'identifiant de l'opération.
	 * @param nom			Le nom de l'opération.
	 * @param debit			Le compte à débiter.
	 * @param credit		Le compte à créditer.
	 * @param jours			Le planning des jours de l'opération à générer,
	 * 						selon les mois.
	 * @param libelle		Le libellé de l'écriture à générer.
	 * @param tiers			Le nom du tiers dans l'écriture à générer.
	 * @param pointer		<code>true</code> si les écritures générées doivent
	 * 						être pointées par défaut.
	 * @param dependance	L'opération dont dépend celle-ci.
	 * @param taux			Le taux à appliquer par rapport au montant de
	 * 						<code>dependance</code>.
	 */
	public PermanentProport(Integer id, String nom, Compte debit, Compte credit,
			String libelle, String tiers, boolean pointer,
			Map<Month, Integer> jours, Permanent dependance, BigDecimal taux) {
		super(id, nom, debit, credit, libelle, tiers, pointer, jours);
		
		// Ne peut pas dépendre de soi-même
		if (dependance == this)
			throw new IllegalArgumentException();
		
		this.dependance = dependance;
		this.taux = taux;
	}// constructeur
	
	@Override
	BigDecimal getMontant(Month month)
			throws EcritureMissingArgumentException,
			InconsistentArgumentsException {
		return dependance.getMontant(month)			// Montant de base
				.multiply(taux)						// Multiplier par le taux
				.movePointLeft(2)					// %: Diviser par 100
				.setScale(2, RoundingMode.HALF_UP); // Arrondir au centime
	}// getMontant

}
