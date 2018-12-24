/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.table;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.ctrl.EcritureDraft;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.DataObservable;
import haas.olivier.comptes.gui.actions.DataObserver;
import haas.olivier.comptes.gui.actions.MonthObservable;

/**
 * Un <code>TableModel</code> pour lister les écritures d'un compte sur une
 * période donnée.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class EcrituresTableModel extends FinancialTableModel
implements DataObserver {

	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(EcrituresTableModel.class.getName());
	
	/**
	 * Modèle définissant la disposition normale des colonnes.
	 */
	private static final ColumnType[] normalDisposition =
		{ColumnType.DATE, ColumnType.POINTAGE, ColumnType.TIERS,
		ColumnType.LIBELLE, ColumnType.CHEQUE, ColumnType.MONTANT,
		ColumnType.CONTREPARTIE};
	
	/**
	 * Modèle définissant la disposition des colonnes pour la vue du compte
	 * abstrait d'épargne.
	 */
	private static final ColumnType[] epargneDisposition =
		{ColumnType.DATE, ColumnType.TIERS, ColumnType.LIBELLE,
		ColumnType.MONTANT, ColumnType.CONTREPARTIE, ColumnType.COMPTE};
	
	/**
	 * Nombre minimum de lignes à afficher (sous réserve d'écritures dispo).
	 */
	private static final int LIGNES_MINI = 50; 

	/**
	 * Drapeau indiquant si les écritures doivent être triées par pointage.
	 * <p>
	 * Si <code>false</code>, alors elles sont triées par ordre naturel.
	 */
	public static boolean triPointage = false;

	/**
	 * Modifie la disposition des colonnes.
	 */
	public void setDisposition(ColumnType[] disposition) {
		this.disposition = disposition;
		this.fireTableStructureChanged();			// Remettre à jour la vue
	}
	
	/**
	 * Les modèles des écritures comptables à afficher.
	 */
	protected final ArrayList<EcritureRowModel> rowModels = new ArrayList<>();

	/**
	 * Observable de changements de données.
	 */
	private DataObservable dataObservable;
	
	/**
	 * Construit un modèle de table d'écritures.
	 * 
	 * @param compte
	 *            Le compte à cibler
	 * @param month
	 *            Le mois à afficher
	 */
	public EcrituresTableModel(MonthObservable monthObservable,
			CompteObservable compteObservable, DataObservable dataObservable) {
		super(monthObservable, compteObservable);
		this.dataObservable = dataObservable;
		
		// S'enregistrer auprès du DataObservable
		dataObservable.addObserver(this);
		
		// Disposition par défaut
		disposition = normalDisposition;
	}

	/**
	 * Renvoie un modèle de ligne d'écriture adapté à la vue.
	 * <p>
	 * Cette méthode peut être réécrite par une classe dérivée pour modifier le
	 * comportement d'affichage des <code>Ecriture</code>s dans un autre
	 * contexte.
	 * 
	 * @param e	L'écriture à contrôler.
	 */
	protected EcritureRowModel getEcritureRowModel(Ecriture e) {
		return  new EcritureRowModel(e, compte);
	}
	
	/**
	 * Met à jour le modèle.
	 * <p>
	 * Les écritures utilisées sont celles du mois cible, complétées si besoin
	 * des écritures immédiatement antérieures pour atteindre le minimum
	 * souhaité à l'affichage LIGNES_MINI, en restant toutefois une période de
	 * 12 mois.<br>
	 * Les écritures doivent concerner le compte cible (pour le compte abstrait
	 * d'épargne: avoir une influence sur l'épargne). Les écritures non pointées
	 * ne sont affichées que sur des mois postérieurs ou égaux à la date
	 * système.
	 */
	@Override
	public void update() {
		LOGGER.config("Rafraîchissement des données...");
		
		// Ajuster la disposition des colonnes
		if (compte != null && compte.getType() == TypeCompte.SUIVI_EPARGNE) {
			// Onglet épargne
			
			// Utiliser la disposition spécifique pour l'épargne 
			if (disposition != epargneDisposition) {
				setDisposition(epargneDisposition);
			}
			
		} else {
			
			// Disposition normale
			if (disposition != normalDisposition) {
				setDisposition(normalDisposition);
			}
		}

		Month month = MonthObservable.getMonth();	// Le mois sélectionné
		Month today = Month.getInstance();			// Le mois actuel

		try {
			// Créer les modèles des lignes d'écritures à afficher
			rowModels.clear();						// Effacer l'existant
			rowModels.add(							// Modèle de saisie
					new EcritureRowModel(null, compte));
			
			// Récupérer les écritures
			EcritureDAO dao = DAOFactory.getFactory().getEcritureDAO();
			Iterable<Ecriture> source = triPointage	// Écritures avant fin mois
					? dao.getPointagesTo(month)		// triées par pointages
					: dao.getAllTo(month);			// ou par ordre naturel
			for (Ecriture e : source) {
				
				// Date à prendre en compte
				Date date = triPointage ? e.pointage : e.date;

				// Vérifier si l'écriture est utile (cf. javadoc)
				if (
						// Il y a un compte sélectionné !
						compte != null
						
						// et l'écriture impacte le compte cible
						&& compte.getViewSign(e.debit, e.credit) != 0
						
						// Et:
						&&
							/* Si on n'a pas de date (cas des écritures non
							 * pointées), on est sur un mois postérieur ou égal
							 * au mois en cours
							 */
							(date != null || !month.before(today))
							
							// Et dans le mois cible ou pas assez d'écritures
							&& (month.includes(date)
									|| rowModels.size() < LIGNES_MINI)) {
					
					// Ajouter cette écriture
					rowModels.add(getEcritureRowModel(e));
				}
			}

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Impossible de lire les données", e);
		}

		// Mettre à jour l'affichage
		fireTableDataChanged();
		LOGGER.config("Prêt");
	}

	@Override
	public int getRowCount() {
		return rowModels.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		return rowModels.get(row).get(disposition[col]);
	}
	
	/**
	 * Modifie une valeur de la table.
	 * <p>
	 * Si la modification concerne une écriture existante, l'écriture est mise à
	 * jour dans le DAO.<br>
	 * Si la modification concerne la première ligne, la méthode crée une
	 * écriture et l'insère dans le DAO. S'il manque des données essentielles à
	 * l'instanciation de l'écriture, les données saisies sont simplement
	 * stockées dans le modèle et la méthode se termine immédiatement, sans
	 * instanciation d'écriture ni information du DAO.
	 */
	@Override
	public void setValueAt(Object newValue, int row, int col) {

		// Récupérer le modèle de la ligne d'écriture concernée
		EcritureRowModel rowModel =					// Modèle de la ligne
				rowModels.get(row);

		// Appliquer la modification
		rowModel.set(newValue, disposition[col]);	// Effectuer la modification

		try {
			// Tenter d'instancier une écriture et de l'ajouter au modèle
			EcritureController.insert(rowModel.createEcriture());
			
			/*
			 * Prévenir du changement de données.
			 * Il se peut toutefois que la mise à jour soit impossible, si on
			 * n'a pas pu déterminer une date à partir de laquelle mettre à
			 * jour.
			 */
			dataObservable.notifyObservers();
			
		} catch (EcritureMissingArgumentException e1) {
			// Tant pis pour l'instant
			
		} catch (InconsistentArgumentsException | IOException e1) {
			LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
		}
	}

	/**
	 * Renvoie l'écriture correspondant à la ligne en question.
	 * 
	 * @param row
	 *         Le numéro de ligne dans le modèle
	 *         
	 * @return L'écriture correspondante, ou null s'il n'y en a pas. NB: la
	 *         première ligne du modèle est toujours libre pour la saisie.
	 */
	public Ecriture getEcritureAt(int row) {
		return rowModels.get(row).getEcriture();
	}

	/**
	 * Renvoie le montant à afficher dans la ligne donnée.
	 * <p>
	 * Il s'agit de l'impact de cette écriture sur le solde du compte.<br>
	 * Suivant que le compte figure au crédit ou au débit, qu'il s'agit d'un
	 * compte bancaire ou budgétaire, le montant sera positif ou négatif.
	 * 
	 * @return	L'impact de l'écriture de cette ligne sur le compte, ou zéro si
	 * 			la ligne de contient aucune écriture.
	 * @see Compte.getImpactOf(Ecriture e) */
	@Override
	public BigDecimal getMontantAt(int row) {
		Ecriture e = getEcritureAt(row);			// Ecriture de cette ligne
		return e == null ? BigDecimal.ZERO : compte.getImpactOf(e);
	}
	
	/**
	 * Rend la table entièrement éditable, sauf pour la liste des écritures
	 * d'épargne.
	 * 
	 * @return	true sauf si le compte visualisé est le compte abstrait
	 * 			d'épargne. */
	@Override
	public boolean isCellEditable(int row, int column) {
		return (compte == null || compte.getType() != TypeCompte.SUIVI_EPARGNE);
	}

	@Override
	public void dataModified() {
		update();			// Mettre à jour quand les données sont modifiées
	}
	
	DataObservable getDataObservable() {
		return dataObservable;
	}
}// class EcrituresTableModel

/**
 * Un modèle gérant les données d'une écriture, pour permettre leur affichage et
 * leur modification dans une ligne de la table.
 * <p>
 * Cette classe fait simplement le lien entre les types de colonnes et les
 * données d'un brouillon d'écriture.
 * 
 * @see {@link haas.olivier.comptes.ctrl.EcritureDraft}
 *
 * @author Olivier HAAS
 */
class EcritureRowModel {

	/**
	 * Un objet contenant les données de l'écriture à représenter.
	 */
	private final EcritureDraft draft;
	
	/**
	 * Un drapeau indiquant si l'écriture est lue "à l'envers".<br>
	 * Suivant que le compte depuis lequel est lue l'écriture est le compte
	 * débité ou du compte crédité, le montant apparaîtra dans un sans ou dans
	 * l'autre, et le compte à afficher en tant que contrepartie sera le compte
	 * crédité ou le compte débité, respectivement.
	 */
	private final boolean inverse;
	
	/**
	 * Drapeau indiquant si le compte visualisé est un compte budgétaire.<br>
	 * Si <code>true</code>, alors le montant doit être inversé car les comptes
	 * budgétaires enregistrent les montants à l'envers.<br>
	 * Si par ailleurs l'écriture n'est pas "à l'endroit", alors cela fait une
	 * double inversion du montant et donc le montant affiché est le même que
	 * celui de l'écriture.
	 */
	private final boolean visuBudget;
	
	/**
	 * Construit un modèle de ligne d'écriture.
	 * <p>
	 * Si l'<code>Ecriture</code> est <code>null</code>, les données sont
	 * initialisées avec au crédit le compte spécifié.
	 * 
	 * @param e		L'<code>Ecriture</code> à contrôler.<br>
	 * 				Si l'argument est <code>null</code>, tous les paramètres de
	 * 				l'objet sont <code>null</code>.
	 * 
	 * @param visu	Le compte visualisé. L'affichage doit donc faire apparaître
	 * 				l'impact par rapport à ce compte.
	 */
	EcritureRowModel(Ecriture e, Compte visu) {
		draft = new EcritureDraft(e);					// Brouillon d'écriture
		
		// Si l'écriture est null, définir le compte visualisé au crédit
		if (e == null)
			draft.credit = visu;
		
		// Arrivé ici, les comptes sont bien définis dans draft
		inverse = (visu == draft.debit);
		visuBudget = (visu != null) && visu.getType().isBudgetaire();
	}
	
	/**
	 * Renvoie la date de l'écriture en cours de saisie.
	 * 
	 * @return	La date de l'écriture, ou <code>null</code> si la date n'est pas
	 * 			saisie.
	 */
	Date getDate() {
		return draft.date;
	}
	
	/**
	 * Renvoie la donnée du type spécifié.
	 * 
	 * @param type	Le type de la donnée voulue.
	 * 
	 * @return		Une <code>String</code>, ou un <code>BigDecimal</code>, ou
	 * 				un <code>Compte</code>, ou une <code>Date</code>, ou un
	 * 				<code>Integer</code> selon le cas.
	 */
	Object get(ColumnType type) {
		switch (type) {
		case IDENTIFIANT:	return draft.id;
		case DATE:			return draft.date;
		case DATE_POINTAGE:	return draft.pointage;
		case POINTAGE:		return draft.pointage != null;
		case LIBELLE:		return draft.libelle;
		case TIERS:			return draft.tiers;
		case CHEQUE:		return draft.cheque;
		case MONTANT:		return getMontant();
		case CONTREPARTIE:	return inverse ? draft.credit : draft.debit;
		case COMPTE:		return inverse ? draft.debit : draft.credit;
		default:			return null;
		}
	}
	
	/**
	 * Modifie une donnée dans le modèle de ligne d'écriture.
	 * 
	 * @param value	La nouvelle valeur.
	 * @param type	Le type de la valeur modifiée.
	 */
	void set(Object value, ColumnType type) {
		switch (type) {
		case CHEQUE:		draft.cheque	= (Integer) value;		break;
		case DATE:			draft.date		= (Date) value;			break;
		case LIBELLE:		draft.libelle	= value.toString();		break;
		case TIERS:			draft.tiers		= value.toString();		break;
		case POINTAGE:	// Même effet que DATE_POINTAGE
		case DATE_POINTAGE:	draft.pointage	= (Date) value;			break;
		
		case MONTANT:
			draft.montant = (inverse == visuBudget)
					? (BigDecimal) value
					: ((BigDecimal) value).negate();
			break;
			
		case COMPTE:
			if (inverse) {
				draft.debit = (Compte) value;
			} else {
				draft.credit = (Compte) value;
			}
			break;
			
		case CONTREPARTIE:
			if (inverse) {
				draft.credit = (Compte) value;
			} else {
				draft.debit = (Compte) value;
			}
			break;
			
		default:
		}
	}
	
	/**
	 * Renvoie le montant à afficher.
	 * 
	 * @return	Le montant si l'écriture est à l'endroit et que le compte
	 * 			visualisé est un compte bancaire, ou son opposé si l'écriture
	 * 			est à l'envers OU (exclusif) si le compte visualisé est un
	 * 			compte budgétaire.<br>
	 * 			Si l'écriture est à l'envers ET que le compte est budgétaire,
	 * 			c'est une double inversion et donc on renvoie aussi le montant
	 * 			original.
	 */
	BigDecimal getMontant() {
		BigDecimal montant = draft.montant;				// Montant de l'écriture
		
		// Cas où le montant n'est pas (encore) défini : renvoyer null
		if (montant == null)
			return null;
		
		/*
		 * Cas général : écriture à l'endroit et compte bancaire, ou l'inverse.
		 * Sinon on prend l'opposé du montant.
		 */
		return (inverse == visuBudget) ? montant : montant.negate();
	}
	
	/**
	 * Tente d'instancier une écriture à partir des données saisies. 
	 * 
	 * @throws EcritureMissingArgumentException
	 * 			Si les données sont insuffisantes pour instancier une écriture.
	 * 			
	 * @throws InconsistentArgumentsException 
	 * 			Si les données sont incohérentes et ne permettent pas
	 * 			d'instancier l'écriture.
	 */
	Ecriture createEcriture() throws EcritureMissingArgumentException,
	InconsistentArgumentsException {
		return draft.createEcriture();
	}
	
	/**
	 * Renvoie l'Ecriture contrôlée.
	 */
	Ecriture getEcriture() {
		return draft.e;
	}

	/**
	 * Définit si le montant à afficher correspond à l'impact sur le compte
	 * crédité.<br>
	 * Sinon, le montant sera affiché à l'envers.
	 * 
	 * @param e		L'<code>Ecriture</code> destinée à être affichée.
	 * @param visu	Le compte dans lequel elle sera visualisée.
	 */
	protected boolean estAlEndroit(Ecriture e, Compte visu) {
		if (visu.getType() == TypeCompte.SUIVI_EPARGNE) {
			
			/*
			 * Dans le cas du compte abstrait d'épargne, l'écriture est à
			 * l'endroit si le compte crédité est le compte d'épargne.
			 */
			return e.credit.isEpargne();
			
		} else {
			// Cas général : dire si le compte visualisé est au crédit
			return e.credit == visu;
		}
	}
}// class EcrituresController