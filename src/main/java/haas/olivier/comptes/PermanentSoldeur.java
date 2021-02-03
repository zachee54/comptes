/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.util.Month;

import java.math.BigDecimal;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * L'état d'une opération permanente dont le montant correspond au solde d'un
 * compte bancaire.<br>
 * Cette classe permet notamment de solder un compte.
 *
 * @author Olivier HAAS
 */
@Entity
public class PermanentSoldeur extends PermanentState {
	private static final long serialVersionUID = 8595871078180688948L;

	/**
	 * L'opération permanente à laquelle appartient cet état.
	 */
	@OneToOne(cascade = CascadeType.PERSIST)
	private Permanent permanent;
	
	protected PermanentSoldeur() {
	}
	
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
