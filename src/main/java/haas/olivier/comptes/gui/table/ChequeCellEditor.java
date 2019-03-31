/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.table;

import haas.olivier.autocompletion.CompletionEditor;
import haas.olivier.autocompletion.DefaultCompletionModel;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.actions.DataObservable;
import haas.olivier.comptes.gui.actions.DataObserver;
import haas.olivier.util.Month;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;

/**
 * Un éditeur de numéros de chèques avec une aide à la saisie.
 * <p>
 * L'éditeur recueille les derniers numéros de chèques et propose le numéro
 * suivant au moment de la saisie.
 *
 * @author Olivier HAAS
 */
class ChequeCellEditor extends CompletionEditor<Integer>
implements DataObserver {
	private static final long serialVersionUID = -4956665626962829163L;
	
	/**
	 * Le mois le plus ancien pour rechercher les numéros de chèques.
	 */
	private static Month limit = Month.getInstance().getTranslated(-6);
	
	/**
	 * Un vérificateur de validité de la valeur actuelle de l'éditeur.
	 * <p>
	 * Il s'agit en fait d'un <code>InputVerifier</code> utilisé habituellement
	 * pour les <code>JTextField</code>, mais qui est étendu ici à l'ensemble de
	 * l'éditeur.<br>
	 * Il se peut en effet qu'il y ait un texte invalide dans le champ saisie,
	 * mais que l'utilisateur sélectionne une valeur valide dans la liste
	 * d'autocomplétion.
	 */
	private InputVerifier verifier = new InputVerifier() {

		@Override
		public boolean verify(JComponent input) {
			Object fieldValue = getField().getValue();
			
			// Autoriser les Integer et les valeurs vides
			if (fieldValue instanceof Integer
					|| (fieldValue instanceof String
							&& ((String) fieldValue).isEmpty())) {
				return true;
			}
			
			// Cas général : essayer de parser et voir si ça marche
			try {
				Integer.parseInt(fieldValue.toString());
				return true;
				
			} catch (NumberFormatException e) {
				return false;
			}
		}
		
	};// classe anonyme InputVerifier
	
	/**
	 * Construit un éditeur de numéros de chèques avec aide à la saisie.
	 */
	ChequeCellEditor(DataObservable obs) {
		super(new DefaultCompletionModel<Integer>(),// Modèle avec saisie
				true);
		obs.addObserver(this);						// Écouter les changements
		
		// Vérifier que la saisie est toujours la représentation d'un Integer
		getField().setInputVerifier(verifier);
		
		// Remplir la liste des numéros de chèques à proposer
		dataModified();
	}
	
	/**
	 * Renvoie la valeur de l'éditeur en le convertissant en
	 * <code>Integer</code> si besoin.
	 * 
	 * @return	Un <code>Integer</code> correspondant à la valeur de l'éditeur,
	 * 			ou <code>null</code> s'il ne s'agit pas d'une représentation
	 * 			valide d'un <code>Integer</code>.
	 */
	@Override
	public Integer getCellEditorValue() {
		Object fieldValue = getField().getValue();// Valeur saisie ou complétée
		
		if (fieldValue instanceof Integer) {
			return (Integer) fieldValue;		// Déjà un Integer : renvoyer
			
		} else {
			try {								// Essayer de parser
				return Integer.parseInt(fieldValue.toString());
				
			} catch (NumberFormatException e) {
				return null;					// Erreur parsage: renvoyer null
			}
		}
	}
	
	/**
	 * Vérifie la validité de la valeur actuelle avant d'arrêter l'édition.<br>
	 * Si la valeur est invalide (ce qui n'arrive en principe que par la saisie
	 * d'un texte ne correspondant pas à la représentation d'un
	 * <code>Integer</code>), l'édition n'est pas arrêtée et le champ de saisie
	 * est entouré d'une bordure rouge.
	 */
	@Override
	public boolean stopCellEditing() {
		
		// Vérifier la saisie
		if (verifier.verify(getField())) {
			
			// Ok : enlever l'éventuelle bordure rouge et valider
			getField().setBorder(null);
			return super.stopCellEditing();
			
		} else {
			
			// Invalide : entourer en rouge et refuser de valider l'édition
			getField().setBorder(BorderFactory.createLineBorder(Color.red));
			return false;
		}
	}

	/**
	 * Enlève la bordure rouge éventuelle avant d'annuler l'édition.
	 */
	@Override
	public void cancelCellEditing() {
		getField().setBorder(null);
		super.cancelCellEditing();
	}
	
	/**
	 * Réactualise la liste des numéros de chèques à proposer en autocomplétion.
	 */
	@Override
	public void dataModified() {
		try {
			// Récupérer les écritures à lire
			Iterable<Ecriture> ecritures =
					DAOFactory.getFactory().getEcritureDAO().getAllSince(limit);
			
			// Collecter les numéros de chèques
			NavigableSet<Integer> nums =			// Numéros trouvés
					new TreeSet<Integer>();
			for (Ecriture e : ecritures) {
				if (e.cheque != null)
					nums.add(e.cheque);
			}
			
			// Déterminer les prochains numéros de chèques possibles
			List<Integer> values =					// Valeurs à suggérer
					new ArrayList<Integer>();
			Iterator<Integer> it =					// Itérateur décroissant
					nums.descendingIterator();
			Integer prec = null;
			while (it.hasNext()) {					// Parcourir les numéros
				Integer num = it.next();
				if (prec == null					// Premier numéro
						|| num + 1 != prec) {		// Ou pas contigu
					values.add(num+1);				// Mémoriser le num d'après
				}
				prec = num;							// Passer au suivant
			}
			
			// Définir ces numéros comme valeurs d'autocomplétion
			Collections.sort(values);				// Trier
			((DefaultCompletionModel<Integer>) model).setValues(values);
			
		} catch (IOException e) {
			e.printStackTrace();
			// TODO Exceptino à gérer
		}
	}
}
