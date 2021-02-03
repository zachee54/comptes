/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * L'état d'une opération permanente qui génère des écritures dont les montants
 * sont proportionnels à ceux d'une autre opération permanente.
 *
 * @author Olivier Haas
 */
@Entity
public class PermanentProport extends PermanentState {
	private static final long serialVersionUID = 8416209118287401779L;


	/**
	 * Écriture permanente dont dépend celle-ci.
	 */
	@ManyToOne(cascade = CascadeType.PERSIST)
	public Permanent dependance;
	
	/**
	 * Coefficient multiplicateur, exprimé en pourcentage.
	 */
	public BigDecimal taux;

	protected PermanentProport() {
	}
	
	/**
	 * Construit un état d'opération permanente qui génère des écritures dont le
	 * montant est proportionnel à une autre opération permanente
	 * 
	 * @param dependance	L'opération dont dépend celle-ci.
	 * @param taux			Le taux à appliquer par rapport au montant de
	 * 						<code>dependance</code>.
	 */
	public PermanentProport(Permanent dependance, BigDecimal taux) {
		this.dependance = dependance;
		this.taux = taux;
	}
	
	@Override
	public BigDecimal getMontant(Month month)
			throws EcritureMissingArgumentException,
			InconsistentArgumentsException {
		return dependance.getMontant(month)			// Montant de base
				.multiply(taux)						// Multiplier par le taux
				.movePointLeft(2)					// %: Diviser par 100
				.setScale(2, RoundingMode.HALF_UP); // Arrondir au centime
	}

}
