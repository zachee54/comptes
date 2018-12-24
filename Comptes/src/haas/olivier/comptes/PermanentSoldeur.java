/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Une opération permanente dont le montant correspond au solde d'un compte
 * bancaire.<br>
 * Cette classe permet notamment de solder un compte.
 *
 * @author Olivier HAAS
 */
public class PermanentSoldeur extends Permanent {
	private static final long serialVersionUID = 4368594994048311019L;

	/**
	 * Construit une opération permanente générant des écritures dont le
	 * montant est égal au solde d'un compte bancaire prédéfini.
	 * 
	 * @param id			L'identifiant de l'opération.
	 * @param nom			Le nom de l'opération.
	 * @param compteASolder	Le compte bancaire à solder.
	 * @param credit		Le compte à créditer.
	 * @param libelle		Le libellé de l'écriture à générer.
	 * @param tiers			Le nom du tiers dans l'écriture à générer.
	 * @param pointer		<code>true</code> si les écritures générées doivent
	 * 						être pointées par défaut.
	 * @param jours			Le planning des jours de l'opération à générer,
	 * 						selon les mois.
	 */
	public PermanentSoldeur(Integer id, String nom,
			Compte compteASolder, Compte credit,
			String libelle, String tiers, boolean pointer,
			Map<Month, Integer> jours) {
		super(id, nom, compteASolder, credit, libelle, tiers, pointer, jours);
	}

	@Override
	BigDecimal getMontant(Month month) {
		return debit.getSoldeAVue(month.getPrevious());
	}

}
