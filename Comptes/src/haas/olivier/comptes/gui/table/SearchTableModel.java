package haas.olivier.comptes.gui.table;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.actions.DataObservable;
import haas.olivier.comptes.gui.actions.MonthObservable;

/** Un modèle de table gérant les résultats d'une recherche parmi les
 * écritures.
 * <p>
 * Le modèle implémente le modèle singleton afin que les résultats soient
 * synchronisés dans tous les onglets.
 *
 * @author Olivier HAAS
 */
// TODO Erreur quand le texte recherché contient des parenthèses
@SuppressWarnings("serial")
public class SearchTableModel extends EcrituresTableModel
implements DocumentListener {

	/** L'instance unique. */
	private static SearchTableModel instance = null;
	
	/** Renvoie le modèle unique.
	 * <p>
	 * Le modèle écoute les changements des observables et du document
	 * spécifiés.
	 */
	public static synchronized SearchTableModel getInstance(
			MonthObservable monthObservable, DataObservable dataObservable,
			Document doc) {
		
		// Instancier si besoin
		if (instance == null) {
			instance = new SearchTableModel(monthObservable, dataObservable);
		} else {
			// Sinon, observer ce MonthObservable avec l'instance
			monthObservable.addObserver(instance);
		}
		return instance;
	}// getInstance
	
	/** Le modèle utilisé pour la saisie du texte à rechercher.
	 * <p>
	 * L'avantage de l'implémenter ici est de forcer un texte unique pour
	 * plusieurs champs de saisie, répartis dans différentes vues de
	 * l'application.
	 */
	private static PlainDocument document = new PlainDocument();
	
	/** Renvoie le modèle unique de champ de saisie. */
	public static Document getDocument() {
		return document;
	}// getDocument
	
	/** Texte recherché. */
	private static String text = null;
	
	/** Construit un modèle de table gérant les résultats de recherche. */
	private SearchTableModel(MonthObservable monthObservable,
			DataObservable dataObservable) {
		// Se mettre à jour en fonction du mois et des données uniquement
		super(monthObservable, null, dataObservable);
		
		// Changer la disposition
		ColumnType[] disposition = {ColumnType.DATE, ColumnType.POINTAGE,
				ColumnType.TIERS, ColumnType.LIBELLE, ColumnType.CHEQUE,
				ColumnType.MONTANT, ColumnType.COMPTE, ColumnType.CONTREPARTIE};
		setDisposition(disposition);
		
		// Écouter les changements dans la saisie utilisateur
		document.addDocumentListener(this);
	}// constructeur

	/** Met à jour la table en recherchant le texte voulu.
	 * 
	 * @param e	L'événement sur le document contenant le texte à rechecher.
	 */
	private void search(DocumentEvent e) {
		Document doc = e.getDocument();					// Le document
		try {
			text = doc.getText(0, doc.getLength());		// Le texte
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		update();										// Mettre à jour
	}// search

	/** Recherche les écritures contenant le texte précédemment défini. */
	@Override
	public void update() {
		rowModels.clear();							// Effacer l'existant
		
		// Si aucun texte de recherche n'est saisi, laisser vide
		if (text == null || text.isEmpty())
			return;
		
		// Motif recherché
		Pattern pattern = Pattern.compile(			// Expression compilée
				text.toLowerCase());				// en minuscules
		
		// Obtenir les écritures contenant le motif recherché depuis 1 an
		Month month = MonthObservable.getMonth();	// Le mois sélectionné
		try {
			// Obtenir toutes les écritures
			Iterable<Ecriture> ecritures =			// Toutes les écritures
					DAOFactory.getFactory().getEcritureDAO().getAllBetween(
							month.getTranslated(-11),//Depuis un an
							month);					// Jusqu'au mois choisi
			
			// Tester le motif
			for (Ecriture e : ecritures) {			// Tester chaque écriture
				if (e.matches(pattern)) {			// Trouvé une
					rowModels.add(					// Ajouter un contrôleur
							getEcritureRowModel(e));
				}// if matches
			}// for all
			
		} catch (IOException e) {					// Erreur: ne rien renvoyer
		}// try
		fireTableDataChanged();
	}// update

	/** Met à jour les données dans tous les cas, alors que la classe mère ne se
	 * met à jour que si un compte est sélectionné.
	 */
	@Override
	public void monthChanged(Month month) {
		update();
	}// monthChanged

	/** Renvoie un modèle de lignes d'Ecriture adapté à la vue "recherche". */
	@Override
	protected SearchRowModel getEcritureRowModel(Ecriture e) {
		return new SearchRowModel(e);
	}// getEcritureController
	
	/** Retourne le montant affiché sur cette ligne. */
	@Override
	public BigDecimal getMontantAt(int row) {
		BigDecimal montant =
				(BigDecimal) rowModels.get(row).get(ColumnType.MONTANT);
		return montant == null ? BigDecimal.ZERO : montant;
	}// getMontantAt
	
	// Interface DocumentListener
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		search(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		search(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}
}// public class RechercheTableModel

/** Un contrôleur d'écritures pour afficher les écritures dans le bon sens dans
  * la vue "Recheche". 
  * 
  * @author Olivier Haas
  */
class SearchRowModel extends EcritureRowModel {

	/** Construit un contrôleur d'écritures pour afficher les écritures dans le
	 * bon sens dans la vue "Recherche".
	 * 
	 * @param e	L'écriture à contrôler.
	 */
	SearchRowModel(Ecriture e) {
		super(e, null);
	}// constructeur
	
	/** Cette méthode permet de présenter préférentiellement les comptes
	 * bancaires au crédit, ce qui permet de lire les écritures dans un sens
	 * plus naturel.
	 * <p>
	 * S'il s'agit de deux comptes bancaires ou deux comptes budgétaires, elle
	 * privilégie les comptes en fonction de la valeur de leur type: un compte
	 * courant apparaîtra au crédit plutôt qu'un compte d'épargne ; un compte
	 * d'épargne plutôt qu'un compte d'emprunt, etc.
	 */
	@Override
	protected boolean estAlEndroit(Ecriture e, Compte visu) {
		if (e.debit instanceof CompteBancaire
				&& e.credit instanceof CompteBudget) {
			return false;	// Changer pour avoir le compte bancaire au crédit 
			
		} else if (e.debit instanceof CompteBudget
				&& e.credit instanceof CompteBancaire) {
			return true;	// Laisser le compte bancaire au crédit
			
		} else {
			// Le type le plus significatif (plus petit) au crédit
			return e.credit.getType().level < e.debit.getType().level;
		}
	}// estAlEndroit
	
}// class SearchController