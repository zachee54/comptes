/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import haas.olivier.util.Month;

/**
 * L'interface d'état des opérations permanentes. Elle détermine la façon de
 * définir une nouvelle écriture.
 *
 * @author Olivier Haas
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PermanentState implements Serializable {
	private static final long serialVersionUID = 8422661694947570809L;
	
	/**
	 * L'identifiant unique.
	 */
	@Id
	@GeneratedValue
	private Integer id;
	
	protected PermanentState() {
	}
	
	/**
	 * Renvoie l'identifiant unique.
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Modifie l'identifiant unique.
	 */
	public void SetId(Integer id) {
		this.id = id;
	}
	
	
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
