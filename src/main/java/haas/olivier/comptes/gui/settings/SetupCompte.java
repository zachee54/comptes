/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;
import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.SimpleGUI;
import haas.olivier.comptes.gui.table.FinancialTable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * Une boîte de dialogue pour paramétrer les comptes.
 * 
 * @author Olivier HAAS
 */
public class SetupCompte {
	
	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SetupCompte.class.getName());
		
	/**
	 * Action pour quitter.
	 */
	private final ActionListener quitActionListener =
			EventHandler.create(ActionListener.class, this, "askAndQuit");
	
	/**
	 * Le GUI principal.
	 */
	private final SimpleGUI gui;
	
	/**
	 * La boîte de dialogue.
	 */
	private final JDialog dialog;

	/**
	 * Le contrôleur de l'éditeur de compte.
	 */
	private final CompteEditorController controller;
	
	/**
	 * La liste graphique des comptes.
	 */
	private final JList<Compte> listComptes;
	
	/**
	 * Construit une boîte de dialogue de gestion des comptes.
	 * 
	 * @param gui	L'instance du GUI en cours.
	 * @param owner	Le cadre auquel rattacher la boîte de dialogue.
	 */
	public SetupCompte(SimpleGUI gui, JFrame owner) {
		this.gui = gui;
		
		CompteEditor editor = new CompteEditor();
		controller = new CompteEditorController(editor);
		listComptes = createComptesList();
		fillComptesList();
		UniversalAction actionQuitter = new UniversalAction(quitActionListener);
		
		// Panneau principal
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(createListComptesPanel(), BorderLayout.WEST);
		main.add(createEditionPanel(editor));
		main.add(createValidationPanel(), BorderLayout.SOUTH);
		setActionOnEscape(actionQuitter, main);
		
		// Fenêtre principale
		dialog = new JDialog(owner, "Gestion des comptes");
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(actionQuitter);
		dialog.add(main);
		dialog.pack();
		dialog.setLocationRelativeTo(null);					// Centrer
		dialog.setVisible(true);
	}
	
	/**
	 * Renvoie une liste des comptes triée.
	 * 
	 * @return	Une nouvelle liste des comptes triée.
	 */
	private static Iterable<Compte> getSortedComptes() {
		List<Compte> comptes = new ArrayList<>(getAllComptes());
		Collections.sort(comptes);
		return comptes;
	}

	/**
	 * Renvoie tous les comptes du modèle.
	 * 
	 * @return	Tous les comptes. En cas d'erreur, l'utilisateur est averti et
	 * 			la méthode renvoie une collection vide.
	 */
	private static Collection<Compte> getAllComptes() {
		try {
			return DAOFactory.getFactory().getCompteDAO().getAll();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Erreur lors de la récupération des comptes", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Met à jour les suivis à partir de la date la plus ancienne parmi celles
	 * spécifiées.
	 * 
	 * @param dates	Des dates à partir desquelles mettre à jour les suivis.
	 */
	private static void updateSuivisFrom(Date... dates) {
		Date first = getMinDate(dates);
		if (first != null) {
			try {
				EcritureController.updateSuivis(Month.getInstance(first));
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Échec de mise à jour des suivis", e);
			}
		}
	}
	
	/**
	 * Renvoie la date la plus ancienne parmi les dates spécifiées.
	 * 
	 * @param dates	Des dates. Peuvent être <code>null</code>.
	 * 
	 * @return		La date la plus ancienne, ou <code>null</code> si toutes les
	 * 				dates spécifiées sont <code>null</code>.
	 */
	private static Date getMinDate(Date... dates) {
		Date first = null;
		for (Date date : dates) {
			if (first == null || (date != null && first.after(date))) {
				first = date;
			}
		}
		return first;
	}

	/**
	 * Définit l'action à exécuter lorsque l'utilisateur appuie sur la touche
	 * Echap.
	 * 
	 * @param action	L'action à exécuter.
	 * @param component	Le composant sur qui repose la détection de la touche.
	 */
	private static void setActionOnEscape(Action action, JComponent component) {
		component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitter");
		component.getActionMap().put("quitter", action);
	}

	/**
	 * Crée une liste de comptes et écoute les changements de sélection.
	 * 
	 * @return	Une nouvelle liste graphique des comptes.
	 */
	private JList<Compte> createComptesList() {
		JList<Compte> list = new JList<>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(EventHandler.create(
				ListSelectionListener.class, this, "setCompte",
				"source.selectedValue"));
		return list;
	}

	/**
	 * Re-remplit la liste graphique des comptes.
	 */
	private void fillComptesList() {
		
		// Tout insérer dans le modèle
		DefaultListModel<Compte> listModel = new DefaultListModel<>();
		for (Compte compte : getSortedComptes())
			listModel.addElement(compte);
		listComptes.setModel(listModel);
		
		// Sélectionner le bon item
		listComptes.setSelectedValue(controller.getCompte(), true);
	}

	/**
	 * Crée un panneau défilable contenant la liste des comptes.
	 * 
	 * @return	Un nouveau panneau défilable contenant la liste des comptes.
	 */
	private Component createListComptesPanel() {
		JButton nouveauButton = new JButton("Nouveau");
		nouveauButton.addActionListener(EventHandler.create(
				ActionListener.class, listComptes, "clearSelection"));
		
		JScrollPane scrollList = new JScrollPane(listComptes);
		scrollList.setPreferredSize(					// Largeur préférée
				new Dimension(150, scrollList.getPreferredSize().height));
		
		Box listBox = Box.createVerticalBox();
		listBox.add(nouveauButton);
		listBox.add(scrollList);
		return listBox;
	}
	
	/**
	 * Crée un panneau contenant l'éditeur de compte et des boutons de
	 * validation/suppression.
	 * 
	 * @param	L'éditeur de compte.
	 * 
	 * @return	Un nouveau panneau graphique.
	 */
	private Component createEditionPanel(CompteEditor editor) {
		JButton validateButton = new JButton("Appliquer");
		validateButton.addActionListener(EventHandler.create(
				ActionListener.class, this, "apply"));
		
		JButton deleteButton = new JButton("Supprimer");
		deleteButton.addActionListener(EventHandler.create(
				ActionListener.class, this, "confirmDeletion"));
		
		Box validationBox = Box.createHorizontalBox();
		validationBox.add(Box.createHorizontalGlue());
		validationBox.add(validateButton);
		validationBox.add(deleteButton);
		
		JPanel editionPanel = new JPanel(new BorderLayout());
		editionPanel.add(editor.getComponent());
		editionPanel.add(validationBox, BorderLayout.SOUTH);
		return editionPanel;
	}
	
	/**
	 * Crée un panneau horizontal contenant les boutons d'action.
	 * 
	 * @return	Le composant graphique contenant tous les boutons.
	 */
	private Component createValidationPanel() {
		JButton appliquer = new JButton("OK");
		JButton quitter = new JButton("Quitter");
		appliquer.addActionListener(EventHandler.create(
				ActionListener.class, this, "applyAndQuit"));
		quitter.addActionListener(quitActionListener);

		// Barre contenant les boutons
		Box bar = Box.createHorizontalBox();
		bar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bar.add(Box.createHorizontalGlue());
		bar.add(appliquer);
		bar.add(quitter);
		return bar;
	}
	
	/**
	 * Demande au contrôleur d'afficher dans l'éditeur les propriétés du compte
	 * spécifié.
	 * <p>
	 * Si le compte actuellement édité a été modifié, l'utilisateur en est
	 * averti et peut choisir de sauvegarder les modifications, de les
	 * abandonner, ou d'annuler le changement de compte.
	 * 
	 * @param compte	Le compte à afficher.
	 */
	public void setCompte(Compte compte) {
		
		// En cas de callback, on peut revenir ici sur le compte déjà édité
		if (compte == controller.getCompte())
			return;
		
		if (controller.isModified()) {
			switch (askSaveActualCompte()) {
			
			case JOptionPane.CANCEL_OPTION:
				
				// Revenir sur le compte qui était en cours d'édition
				listComptes.setSelectedValue(controller.getCompte(), true);
				return;
			
			case JOptionPane.YES_OPTION:
				apply();
				break;
				
			default:	// Abandonner les modifications = faire comme si de rien
			}
		}
		
		controller.setCompte(compte);
	}
	
	/**
	 * Demande confirmation à l'utilisateur avant de supprimer un compte. Si
	 * l'utilisateur confirme, le compte est effectivement supprimé.
	 */
	public void confirmDeletion() {
		Compte compte = controller.getCompte();
		int confirm = JOptionPane.showConfirmDialog(
				dialog,
				String.format("Voulez-vous vraiment supprimer le compte%n%s ?",
						compte),
				"Supprimer un compte",
				JOptionPane.YES_NO_OPTION);
		
		if (confirm == JOptionPane.YES_OPTION)
			delete(compte);
	}

	/**
	 * Supprime le compte spécifié du modèle.
	 * 
	 * @param compte	Le compte à supprimer.
	 */
	private void delete(Compte compte) {
		try {
			DAOFactory.getFactory().getCompteDAO().remove(compte);
			controller.setCompte(null);
			fillComptesList();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Impossible de supprimer " + compte, e);
		}
	}

	/**
	 * Applique l'ensemble des modifications si nécessaire, et ferme la boîte de
	 * dialogue.
	 */
	public void applyAndQuit() {
		if (!controller.isModified() || apply())
			quit();
	}

	/**
	 * Valide les modifications du compte actuel et met à jour les suivis.
	 * 
	 * @return	<code>true</code> si les modifications ont été appliquées avec
	 * 			succès.
	 */
	public boolean apply() {
		
		// Date du compte avant les changements
		Compte compte = controller.getCompte();
		Date oldOuverture = (compte == null) ? null : compte.getOuverture();
		
		try {
			controller.applyChanges();			// Modifie ou crée le compte
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
			LOGGER.log(Level.FINE, "Impossible de mettre à jour le compte", e);
			return false;
		}
		compte = controller.getCompte();		// compte non null ici
		updateSuivisFrom(oldOuverture, compte.getOuverture());
		
		// Recharger la (nouvelle) liste des comptes
		fillComptesList();
		return true;
	}

	/**
	 * Ferme la boîte de dialogue.<br>
	 * S'il y a des changements non enregistrés, l'utilisateur est invité à
	 * confirmer l'action.
	 */
	public void askAndQuit() {
		if (controller.isModified()) {
			switch (askSaveActualCompte()) {
			case JOptionPane.YES_OPTION:
				if (!apply())
					return;		// Modifications échouées = arrêter tout
				break;
				
			case JOptionPane.CANCEL_OPTION:
				return;			// Annuler = arrêter tout
				
			default:			// Abandonner modifs = faire comme si de rien
			}
		}
		
		quit();
	}
	
	/**
	 * Ouvre une boîte de dialogue demandant à l'utilisateur s'il faut
	 * sauvegarder les modifications du compte actuellement édité, les
	 * abandonner ou ne rien faire.
	 * 
	 * @return	{@link JOptionPane#YES_OPTION} pour sauvegarder,
	 * 			{@link JOptionPane#NO_OPTION} pour abandonner les modifications,
	 * 			{@link JOptionPane#CANCEL_OPTION} pour ne rien faire.
	 */
	private int askSaveActualCompte() {
		Compte actualCompte = controller.getCompte();
		String nomActuel = 
				actualCompte == null ? "en cours" : actualCompte.getNom();
		
		return JOptionPane.showConfirmDialog(
				dialog,
				String.format(
						"Le compte %s a été modifié.%nVoulez-vous sauvegarder les changements ?",
						nomActuel),
				"Compte modifié",
				JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	/**
	 * Ferme la boîte de dialogue.
	 */
	private void quit() {
		dialog.dispose();
		gui.createTabs();							// Recréer les onglets
		gui.dataModified();							// Prévenir du changement
	
		// Mettre à jour la liste des comptes dans le TableCellEditor
		FinancialTable.updateComptesEditor();
	}
}