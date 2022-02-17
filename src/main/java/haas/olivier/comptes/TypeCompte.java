/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

/**
 * Les différents types de comptes possibles.
 */
public enum TypeCompte {
	DEPENSES			(1, false, false, true, "Dépenses"),
	DEPENSES_EN_EPARGNE	(2, true, false, true, "Dépenses (épargne)"),
	RECETTES			(3, false, false, false, "Recettes"),
	RECETTES_EN_EPARGNE	(4, true, false, false, "Recettes (épargne)"),
	COMPTE_COURANT		(5, false, true, false, "Compte courant"),
	COMPTE_CARTE		(6, false, true, false, "Compte carte"),
	COMPTE_EPARGNE		(7, true, true, false, "Épargne"),
	EMPRUNT				(8, false, true, false, "Emprunt"),
	ENFANTS				(9, true, true, false, "Enfants"),
	
	/* Un type spécial pour le compte abstrait d'épargne */
	SUIVI_EPARGNE		(-1, false, false, false, null);
	
	/**
	 * L'identifiant du type, utile pour faciliter l'export autre que
	 * sérialisation et garder une compatibilité en cas de modifications de
	 * l'énumération.
	 */
	// TODO Identifiant inutile ?
	public final int id;
	
	/**
	 * Le nom du type de comptes.
	 */
	public final String nom;
	
	/**
	 * Indique si le type correspond à un compte d'épargne.
	 */
	private final boolean epargne;
	
	/**
	 * Indique si le type correspond à un compte bancaire. Sinon, c'est qu'il
	 * correspond à un compte budgétaire.
	 */
	private final boolean bancaire;
	
	/**
	 * Indique si le type correspond à un compte budgétaire de dépenses.
	 */
	private final boolean depense;
	
	/**
	 * Construit un nouveau type de compte.
	 * 
	 * @param id		L'identifiant unique.
	 * @param epargne	<code>true</code> s'il s'agit d'un type d'épargne.
	 * @param bancaire	<code>true</code> s'il s'agit d'un type de compte
	 * 					bancaire.
	 * @param depense	<code>true</code> s'il s'agit d'un type de compte
	 * 					bugétaire de dépenses. 
	 * @param nom		Le nom du type.
	 */
	private TypeCompte(int id, boolean epargne, boolean bancaire,
			boolean depense, String nom) {
		this.id = id;
		this.nom = nom;
		this.epargne = epargne;
		this.bancaire = bancaire;
		this.depense = depense;
	}
	
	/**
	 * Détermine si ce type correspond à des comptes d'épargne.
	 */
	public boolean isEpargne() {
		return epargne;
	}
	
	/**
	 * Détermine si ce type correspond à des comptes bancaires.
	 */
	public boolean isBancaire() {
		return bancaire;
	}

	/**
	 * Détermine si ce type correspond à des comptes budgétaires.
	 */
	public boolean isBudgetaire() {
		return !bancaire;
	}
	
	/**
	 * Détermine si ce type correspond à des comptes budgétaires de dépenses.
	 */
	public boolean isDepense() {
		return depense;
	}
	
	@Override
	public String toString() {
		return nom;
	}
}
