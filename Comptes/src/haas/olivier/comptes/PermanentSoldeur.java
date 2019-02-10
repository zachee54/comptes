/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;

/**
 * L'état d'une opération permanente dont le montant correspond au solde d'un
 * compte bancaire.<br>
 * Cette classe permet notamment de solder un compte.
 *
 * @author Olivier HAAS
 */
public class PermanentSoldeur implements PermanentState {

	/**
	 * Le compte à solder.
	 */
	private final Compte compteASolder;
	
	/**
	 * Construit un état d'opération permanente générant des écritures dont le
	 * montant est égal au solde d'un compte prédéfini.
	 * 
	 * @param compteASolder	Le compte à solder. Il s'agit en principe d'un
	 * 						compte bancaire, quoique ce ne soit pas obligatoire.
	 */
	public PermanentSoldeur(Compte compteASolder) {
		this.compteASolder = compteASolder;
	}

	@Override
	public BigDecimal getMontant(Month month) {
		return compteASolder.getSoldeAVue(month.getPrevious());
	}

}
