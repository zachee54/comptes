/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.table;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.CompteObserver;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.actions.MonthObserver;

import java.math.BigDecimal;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

/**
 * Un <code>TableModel</code> adapté pour la présentation de tables financières.
 * <p>
 * Il contient soit des écritures, soit des montants de suivi.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
abstract class FinancialTableModel extends AbstractTableModel implements
		MonthObserver, CompteObserver {

	/**
	 * La disposition des colonnes, sous forme de tableau de constantes.
	 */
	ColumnType[] disposition;
	
	/**
	 * Le compte à utiliser.
	 */
	Compte compte;

	/**
	 * Construit un modèle de table financière observant les changements de
	 * mois/date, de compte et de données.
	 * 
	 * @param monthObservable
	 *            Un observable des changements de mois/dates.
	 * @param compteObservable
	 *            Un observable des changements de compte.
	 */
	FinancialTableModel(MonthObservable monthObservable,
			CompteObservable compteObservable) {

		// Écouter les changements
		if (monthObservable != null) {
			monthObservable.addObserver(this);	// de mois/date
		}
		if (compteObservable != null) {
			compteObservable.addObserver(this);	// de compte
			
			// Mémoriser le compte (pas d'accès statique dans CompteObservable)
			compte = compteObservable.getCompte();
		}
	}

	/**
	 * Met à jour les données.
	 */
	// TODO utiliser plutôt fireTableDataChanged ?
	abstract void update();

	/**
	 * Renvoie la disposition actuelle des colonnes, sous forme d'un tableau de
	 * constantes définies dans cette classe.
	 */
	ColumnType[] getDisposition() {
		return disposition;
	}

	@Override
	public int getColumnCount() {
		return (disposition == null) ? 0 : disposition.length;
	}

	@Override
	public String getColumnName(int columnIndex) {

		// Trouver le champ correspondant à la colonne
		switch (disposition[columnIndex]) {
		case IDENTIFIANT:
			return "N°";
		case DATE:
			return "Date";
		case DATE_POINTAGE:
			return "Pointage";
		case POINTAGE:
			return "P";
		case TIERS:
			return "Tiers";
		case LIBELLE:
			return "Commentaire";
		case CHEQUE:
			return "Chèque n°";
		case MONTANT:
			return "Montant";
		case CONTREPARTIE:
			return "Compte";
		case COMPTE:
			return "Compte";
		case MOIS:
			return "Mois";
		case HISTORIQUE:
			return "Solde";
		case AVUE:
			return "Solde à vue";
		case MOYENNE:
			return "Moyenne";
		default:
			return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (disposition[columnIndex]) {

		case IDENTIFIANT:
		case CHEQUE:
			return Integer.class;

		case MONTANT:
		case HISTORIQUE:
		case AVUE:
		case MOYENNE:
			return BigDecimal.class;

		case MOIS:
			return Month.class;

		case DATE:
		case DATE_POINTAGE:
			return Date.class;

		case POINTAGE:
			return Boolean.class;

		default:
			return String.class;
		}
	}
	
	/**
	 * Renvoie le montant à afficher dans la ligne donnée.<br>
	 * Il s'agit du montant qui déterminera la couleur de la ligne.
	 */
	abstract BigDecimal getMontantAt(int row);
	
	
	// Interface CompteObserver
	
	@Override
	public void compteChanged(Compte compte) {
		this.compte = compte;	// Changer le compte
		update();				// Mettre à jour
	}
	
	
	// Interface MonthObserver
	
	@Override
	public void monthChanged(Month month) {
		// Mettre à jour quand le mois est modifié
		if (compte != null)	// Sauf s'il n'y a pas de compte !
			update();
	}

	@Override
	public void dateChanged(Date date) {
		// Les modèles ne réagissent pas aux changements de date.
	}
}