/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.SimpleGUI;
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
 * Une boîte de dialogue pour paramétrer les Permanents. 
 * 
 * @author Olivier HAAS.
 */
public class SetupPermanent {
	
	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SetupPermanent.class.getName());

	/**
	 * Action pour quitter.
	 */
	private final ActionListener quitActionListener =
			EventHandler.create(ActionListener.class, this, "askAndQuit");
	
	/**
	 * Le GUI associé.
	 */
	private final SimpleGUI gui;
	
	/**
	 * Boîte de dialogue principale.
	 */
	private final JDialog dialog;
	
	/**
	 * Le contrôleur qui gère l'éditeur graphique d'opération permanente.
	 */
	private final PermanentEditorController controller;
	
	/**
	 * Liste graphique des opérations permanentes.
	 */
	private final JList<Permanent> listPermanents;
	
	/**
	 * Construit une boîte de dialogue de gestion des <code>Permanent</code>s.
	 * 
	 * @param owner	La fenêtre parent.
	 * @param gui	Le GUI associé (pour mise à jour des boutons).
	 * 
	 * @throws IOException
	 * 				Si une erreur se produit pendant la récupération des
	 * 				comptes ou des <code>Permanent</code>s.
	 */
	public SetupPermanent(JFrame owner, SimpleGUI gui) throws IOException {
		this.gui = gui;

		PermanentEditor editor = new PermanentEditor();
		controller = new PermanentEditorController(editor);
		listPermanents = createPermanentList();
		fillPermanentList();
		UniversalAction actionQuitter = new UniversalAction(quitActionListener);
		
		// Panneau principal
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(createListPermanentsPanel(), BorderLayout.WEST);
		main.add(createEditionPanel(editor));
		main.add(createValidationPanel(), BorderLayout.SOUTH);
		setActionOnEscape(actionQuitter, main);
		
		// Fenêtre principale
		dialog = new JDialog(owner, "Gestion des opérations permanentes");
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(actionQuitter);
		dialog.add(main);
		dialog.setPreferredSize(new Dimension(900,600));
		dialog.pack();
		dialog.setLocationRelativeTo(null);					// Centrer
		dialog.setVisible(true);
	}
	
	/**
	 * Renvoie une collection de tous les <code>Permanent</code>, à laquelle est
	 * ajouté un élément <code>null</code> pour permettre la saisie d'une
	 * nouvelle opération.
	 * 
	 * @return	Une nouvelle collection des <code>Permanent</code>s et d'un
	 * 			élément <code>null</code>.
	 */
	private static Collection<Permanent> getAllPermanentsAndNull() {
		List<Permanent> permanents = new ArrayList<>();
		permanents.addAll(getAllPermanents());
		Collections.sort(permanents);
		permanents.add(0, null);
		return permanents;
	}
	
	/**
	 * Renvoie une liste de l'ensemble des opérations permanentes.
	 * 
	 * @return	Une liste de l'ensemble des opérations permanentes, ou une liste
	 * 			vide si une erreur survient pendant la lecture de données.
	 */
	private static Collection<Permanent> getAllPermanents() {
		try {
			return DAOFactory.getFactory().getPermanentDAO().getAll();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Erreur lors de la récupération des opérations permanentes",
					e);
			return Collections.emptyList();
		}
	}

	/**
	 * Crée une liste graphique des opérations permanentes.
	 * 
	 * @return	Une nouvelle liste graphique des opérations permanentes.
	 */
	private JList<Permanent> createPermanentList() {
		JList<Permanent> list = new JList<>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(EventHandler.create(
				ListSelectionListener.class, this, "setController",
				"source.selectedValue"));
		return list;
	}
	
	/**
	 * Remplace le contenu de la liste graphique par les contrôleurs actuels.
	 * 
	 * @param selected	Le contrôleur
	 */
	private void fillPermanentList() {
		DefaultListModel<Permanent> listModel = new DefaultListModel<>();
		for (Permanent permanent : getAllPermanentsAndNull())
			listModel.addElement(permanent);
		listPermanents.setModel(listModel);
		listPermanents.setSelectedValue(controller, true);
	}

	/**
	 * Crée un panneau défilable contenant la liste des opérations permanentes.
	 * 
	 * @return	Un nouveau panneau défilable contenant la liste des opérations
	 * 			permanentes.
	 */
	private Component createListPermanentsPanel() {
		JButton nouveauButton = new JButton("Nouveau");
		nouveauButton.addActionListener(EventHandler.create(
				ActionListener.class, listPermanents, "clearSelection"));

		JScrollPane scrollList = new JScrollPane(listPermanents);
		scrollList.setPreferredSize(					// Largeur préférée
				new Dimension(150, scrollList.getPreferredSize().height));

		Box listBox = Box.createVerticalBox();
		listBox.add(nouveauButton);
		listBox.add(scrollList);
		return listBox;
	}
	
	/**
	 * Crée la partie centrale de la fenêtre, qui contient l'éditeur et les
	 * boutons de validation/suppression.
	 * 
	 * @param editor	L'éditeur d'opération permanente.
	 * 
	 * @return			Un nouveau composant graphique.
	 */
	private Component createEditionPanel(PermanentEditor editor) {
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
	 * Définit l'action à exécuter lorsque l'utilisateur appuie sur la touche
	 * Echap.
	 * 
	 * @param action	L'action à exécuter.
	 * @param component	Le composant sur qui repose la détection de la touche.
	 */
	private void setActionOnEscape(Action action, JComponent component) {
		component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitter");
		component.getActionMap().put("quitter", action);
	}
	
	/**
	 * Demande au contrôleur d'afficher dans l'éditeur les propriétés de
	 * l'opération permanente spécifiée.
	 * <p>
	 * Si l'opération permanente actuellement éditée a été modifiée,
	 * l'utilisateur en est averti et peut choisir de sauvegarder les
	 * modifications, de les abandonner, ou d'annuler le changement d'opération
	 * permanente.
	 * 
	 * @param permanent	L'opération permanente à afficher.
	 */
	public void setCompte(Permanent permanent) {
		
		// En cas de callback, on peut revenir ici sur l'opération déjà éditée
		if (permanent == controller.getPermanent())
			return;
		
		if (controller.isModified()) {
			switch (askSaveActualPermanent()) {
			
			case JOptionPane.CANCEL_OPTION:
				
				// Revenir sur l'opération qui était en cours d'édition
				listPermanents.setSelectedValue(controller.getPermanent(),true);
				return;
			
			case JOptionPane.YES_OPTION:
				apply();
				break;
				
			default:	// Abandonner les modifications = faire comme si de rien
			}
		}
		
		controller.setPermanent(permanent);
	}
	
	/**
	 * Demande confirmation à l'utilisateur avant de supprimer une opération
	 * permanente. Si l'utilisateur confirme, l'opération est effectivement
	 * supprimée.
	 */
	public void confirmDeletion() {
		Permanent permanent = controller.getPermanent();
		int confirm = JOptionPane.showConfirmDialog(
				dialog,
				String.format(
						"Voulez-vous vraiment supprimer l'opération permanente%n%s ?",
						permanent),
				"Supprimer une opération permanente",
				JOptionPane.YES_NO_OPTION);
		
		if (confirm == JOptionPane.YES_OPTION)
			delete(permanent);
	}

	/**
	 * Supprime l'opération permanente spécifiée du modèle.
	 * 
	 * @param permanent	L'opération permanente à supprimer.
	 */
	private void delete(Permanent permanent) {
		DAOFactory.getFactory().getPermanentDAO().remove(permanent.getId());
		controller.setPermanent(null);
		fillPermanentList();
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
	 * Valide les modifications de l'opération permanente actuelle.
	 * 
	 * @return	<code>true</code> si les modifications ont été appliquées avec
	 * 			succès.
	 */
	public boolean apply() {
		try {
			controller.applyChanges();			// Modifie ou crée l'opération
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
			LOGGER.log(Level.FINE,
					"Impossible de mettre à jour l'opération permanente", e);
			return false;
		}
		
		// Recharger la (nouvelle) liste des comptes
		fillPermanentList();
		return true;
	}

	/**
	 * Ferme la boîte de dialogue.<br>
	 * S'il y a des changements non enregistrés, l'utilisateur est invité à
	 * confirmer l'action.
	 */
	public void askAndQuit() {
		if (controller.isModified()) {
			switch (askSaveActualPermanent()) {
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
	 * sauvegarder les modifications de l'opération permanente actuellement
	 * éditée, les abandonner ou ne rien faire.
	 * 
	 * @return	{@link JOptionPane#YES_OPTION} pour sauvegarder,
	 * 			{@link JOptionPane#NO_OPTION} pour abandonner les modifications,
	 * 			{@link JOptionPane#CANCEL_OPTION} pour ne rien faire.
	 */
	private int askSaveActualPermanent() {
		Permanent actualPermanent = controller.getPermanent();
		String nomActuel = 
				actualPermanent == null ? "en cours" : actualPermanent.getNom();
		
		return JOptionPane.showConfirmDialog(
				dialog,
				String.format(
						"L'opération permanente %s a été modifiée.%nVoulez-vous sauvegarder les changements ?",
						nomActuel),
				"Opération permanente modifiée",
				JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	/**
	 * Ferme la boîte de dialogue.
	 */
	private void quit() {
		dialog.dispose();
		gui.dataModified();
	}
}