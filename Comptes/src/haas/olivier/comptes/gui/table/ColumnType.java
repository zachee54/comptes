/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.table;

/**
 * Énumération de types de colonnes. Ces types de colonnes servent à donner une
 * sémantique aux colonnes des différentes tables d'affichage de données.
 *
 * @author Olivier HAAS
 */
public enum ColumnType {
	
	/**
	 * Type désignant l'identifiant d'une écriture.
	 */
	IDENTIFIANT,
	
	/**
	 * Type désignant la date d'une écriture.
	 */
	DATE,
	
	/**
	 * Type désignant la valeur logique qui indique si une écriture est pointée
	 * ou non.
	 */
	POINTAGE,
	
	/**
	 * Type désignant la date de pointage d'une écriture.
	 */
	DATE_POINTAGE,
	
	/**
	 * Type désignant le tiers.
	 */
	TIERS,
	
	/**
	 * Type désignant le libellé d'une écriture.
	 */
	LIBELLE,
	
	/**
	 * Type désignant le numéro de chèque d'une écriture.
	 */
	CHEQUE,
	
	/**
	 * Type désignant un montant.
	 */
	MONTANT,
	
	/**
	 * Type désignant la contrepartie d'une écriture.<br>
	 * Les écritures étant symétriques, la notion de contrepartie dépend du
	 * contexte dans lequel l'écriture est lue.
	 */
	CONTREPARTIE,
	
	/**
	 * Type désignant le compte mouvementé par une écriture.<br>
	 * Il s'agit du compte considéré comme "principal", selon le contexte.
	 */
	COMPTE,
	
	/**
	 * Type désignant un mois.
	 */
	MOIS,
	
	/**
	 * Type désignant le solde théorique d'un compte.
	 */
	HISTORIQUE,
	
	/**
	 * Type désignant le solde à vue d'un compte bancaire.
	 */
	AVUE,
	
	/**
	 * Type désignant la moyenne glissante d'un compte budgétaire.
	 */
	MOYENNE
}
