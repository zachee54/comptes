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
	 * L'opération permanente à laquelle appartient cet état.
	 */
	private final Permanent permanent;
	
	/**
	 * Construit un état d'opération permanente générant des écritures dont le
	 * montant est égal au solde du compte à débiter.
	 * 
	 * @param permanent	L'opération permanente à laquelle appartient cet état.
	 */
	public PermanentSoldeur(Permanent permanent) {
		this.permanent = permanent;
	}

	@Override
	public BigDecimal getMontant(Month month) {
		return permanent.getDebit().getSoldeAVue(month.getPrevious());
	}

}
