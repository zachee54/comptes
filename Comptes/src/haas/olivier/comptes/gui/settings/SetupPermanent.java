/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.SimpleGUI;
import haas.olivier.comptes.gui.table.ComptesComboBoxRenderer;
import haas.olivier.comptes.gui.table.FinancialTable;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.text.JTextComponent;

/**
 * Une boîte de dialogue pour paramétrer les Permanents. 
 * 
 * @author Olivier HAAS.
 */
public class SetupPermanent {
	
	/**
	 * Un médiateur entre les données de l'interface et les données du modèle.
	 */
	public class DataMediator {
		
		/**
		 * Le contrôleur de données. Il contrôle le <code>Permanent<code> à
		 * éditer.
		 */
		private PermanentController controller;
		
		/**
		 * Drapeau indiquant si l'interface est en cours de mise à jour du fait
		 * du changement de contrôleur.<br>
		 * Dans ce cas, la classe ne doit pas réagir aux événements générés par
		 * les champs de saisie.
		 */
		private boolean updating = false;
		
		/**
		 * Construit un médiateur de données entre l'interface graphique et les
		 * données du modèle.
		 * <p>
		 * L'objet écoute les modifications de tous les champs de saisie et les
		 * répercute sur le contrôleur en cours.
		 */
		private DataMediator() {
			nom.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "nomChanged"));
			libelle.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "libelleChanged"));
			tiers.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "tiersChanged"));
			pointer.addItemListener(EventHandler.create(
					ItemListener.class, this, "setPointer", "stateChange"));
			jours.addTableModelListener(EventHandler.create(
					TableModelListener.class, this, "joursChanged"));
			montants.addTableModelListener(EventHandler.create(
					TableModelListener.class, this, "montantsChanged"));
			taux.addChangeListener(EventHandler.create(
					ChangeListener.class, this, "tauxChanged", ""));
			debit.addItemListener(EventHandler.create(
					ItemListener.class, this, "debitChanged"));
			credit.addItemListener(EventHandler.create(
					ItemListener.class, this, "creditChanged"));
			dependance.addItemListener(EventHandler.create(
					ItemListener.class, this, "dependanceChanged"));
		}
		
		/**
		 * Renvoie le contrôleur vers lequel pointe actuellement le médiateur.
		 */
		PermanentController getController() {
			return controller;
		}
		
		/**
		 * Remplace le contrôleur de référence. Les données du nouveau
		 * contrôleur sont retranscrites dans l'interface graphique.
		 */
		public void setController(PermanentController controller) {
	
			// Ignorer la valeur null
			if (controller == null)
				return;
			
			this.controller = controller;
			updateFromController();
		}
		
		/**
		 * Met à jour l'interface graphique à partir des données du contrôleur.
		 */
		void updateFromController() {
			updating = true;
			typeController.changeVue(controller.getType());
			nom.setText(controller.getNom());
			libelle.setText(controller.getLibelle());
			tiers.setText(controller.getTiers());
			pointer.setSelected(controller.getPointer());
			debit.setSelectedItem(controller.getDebit());
			credit.setSelectedItem(controller.getCredit());
			jours.setMap(controller.getJours());
			montants.setMap(controller.getMontants());
			
			// Taux
			BigDecimal decTaux = controller.getTaux();
			if (decTaux != null)
				taux.setValue(controller.getTaux());	// Taux non null
			/*
			 * Les ItemListener sont notifiés que lorsque la modification vient
			 * de l'utilisateur, pas en cas de changement programmatique. On
			 * peut donc renvoyer vers le contrôleur sans tester le drapeau
			 * updating.
			 */
			
			// Contenu de la combo box de dépendance
			updateDependanceComboBox();
			
			updating = false;
		}
		
		/**
		 * Met à jour la combo box des opérations de dépendance.<br>
		 * Elle supprime le contenu actuel et y insère tous les
		 * <code>Permanent</code>s, sauf celui qui dépend du contrôleur
		 * actuellement sélectionné.
		 * <p>
		 * À l'issue de la méthode, la dépendance actuelle est sélectionnée,
		 * s'il y en a une.
		 */
		private void updateDependanceComboBox() {
			dependance.removeAllItems();
			
			controllers.stream()
			.filter(c -> c != controller)
			.map(PermanentController::getPermanent)
			.forEach(p -> dependance.addItem(p)); 
			
			dependance.setSelectedItem(controller.getDependance());
		}
		
		/**
		 * Met à jour le compte débité dans le contrôleur actuel.
		 */
		public void debitChanged() {
			if (updating)
				return;
			
			Object selectedDebit = debit.getSelectedItem();
			controller.setDebit(
					selectedDebit instanceof Compte
					? (Compte) selectedDebit
					: null);
		}
		
		/**
		 * Met à jour le compte crédité dans le contrôleur actuel.
		 */
		public void creditChanged() {
			if (updating)
				return;
			
			Object selectedCredit = credit.getSelectedItem();
			controller.setCredit(
					selectedCredit instanceof Compte
					? (Compte) selectedCredit
					: null);
		}
		
		/**
		 * Met à jour la dépendance dans le contrôleur actuel.
		 */
		public void dependanceChanged() {
			if (updating)
				return;
			
			Object selectedDependance = dependance.getSelectedItem();
			controller.setDependance(
					selectedDependance instanceof Permanent
					? (Permanent) selectedDependance
					: null);
		}
	
		/**
		 * Met à jour le nom de l'opération permanente dans le contrôleur
		 * actuel.
		 */
		public void nomChanged() {
			if (!updating)
				controller.setNom(nom.getText());
		}
		
		/**
		 * Met à jour le libellé de l'opération permanente dans le contrôleur
		 * actuel.
		 */
		public void libelleChanged() {
			if (!updating)
				controller.setLibelle(libelle.getText());
		}
		
		/**
		 * Met à jour le nom du tiers dans l'opération permanente dans le
		 * contrôleur actuel.
		 */
		public void tiersChanged() {
			if (!updating)
				controller.setTiers(tiers.getText());
		}
		
		/**
		 * Active ou désactive le pointage de l'opération permanente dans le
		 * contrôleur actuel.
		 * 
		 * @param state	{@link java.awt.event.ItemEvent#SELECTED} ou
		 * 				{@link java.awt.event.ItemEvent#DESELECTED}.
		 */
		public void setPointer(int state) {
			if (updating)
				return;
			
			if (state == ItemEvent.SELECTED) {
				controller.setPointer(true);
			} else if (state == ItemEvent.DESELECTED) {
				controller.setPointer(false);
			}
		}
	
		/**
		 * Reçoit les notifications de changements sur le planning des jours et
		 * les renvoie au contrôleur.
		 * <p>
		 * Pour les tables de jours et de montants, la classe
		 * <code>PlannerTableModel</code> utilise une
		 * <code>Map&lt;Month,Object&gt;</code> pour permettre l'héritage entre
		 * les deux tables.<br>
		 * Il faut transférer les entrées de cette Map vers une
		 * <code>Map&lt;Month,Integer&gt;</code> ou
		 * <code>Map&lt;Month,BigDecimal&gt;</code> avant de l'envoyer au
		 * contrôleur de données.
		 */
		public void joursChanged() {
			controller.setJours(
					jours.getMap().entrySet().stream()
					.collect(castValueAndCollect()));
		}	
			
		/**
		 * Reçoit les notifications de changements sur le planning des montants
		 * et les renvoie au contrôleur.
		 * <p>
		 * Pour les tables de jours et de montants, la classe
		 * <code>PlannerTableModel</code> utilise une
		 * <code>Map&lt;Month,Object&gt;</code> pour permettre l'héritage entre
		 * les deux tables.<br>
		 * Il faut transférer les entrées de cette Map vers une
		 * <code>Map&lt;Month,Integer&gt;</code> ou
		 * <code>Map&lt;Month,BigDecimal&gt;</code> avant de l'envoyer au
		 * contrôleur de données.
		 */
		public void montantsChanged() {		
			controller.setMontants(
					montants.getMap().entrySet().stream()
					.collect(castValueAndCollect()));
		}
		
		/**
		 * Renvoie un collecteur qui caste les valeurs de chaque entrée.
		 * 
		 * @return	Un collecteur qui prend un flux de
		 * 			<code>Entry&lt;Month, Object&gt;</code> et renvoie une
		 * 			<code>Map&lt;Month, T&gt;</code>.
		 */
		@SuppressWarnings("unchecked")
		private <T> Collector<Entry<Month, Object>, ?, Map<Month, T>>
		castValueAndCollect() {
			return Collectors.toMap(e -> e.getKey(), e -> (T) e.getValue());
		}
	
		/**
		 * Actualise le taux de l'opération permanente dans le contrôleur
		 * actuel, à partir du montant figurant dans le spinner.
		 */
		public void tauxChanged(ChangeEvent e) {
			controller.setTaux(new BigDecimal(taux.getValue().toString()));
		}
	}// inner class DataMediator

	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SetupPermanent.class.getName());
	
	/**
	 * Le GUI associé.
	 */
	private final SimpleGUI gui;
	
	/**
	 * Boîte de dialogue principale.
	 */
	private final JDialog dialog;
	
	/**
	 * Médiateur entre les données de l'IHM et du modèle.
	 */
	private final DataMediator dataMediator;
	
	/**
	 * La collection ordonnée de contrôleurs de <code>Permanent</code>s.
	 */
	private ArrayList<PermanentController> controllers;
	
	/**
	 * Liste graphique des Permanents. Elle contient en fait des instances de
	 * <code>PermanentController</code>, transparents pour l'utilisateur.
	 */
	private final JList<PermanentController> listPermanents;
	
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
		
		/*
		 * Le médiateur de données ne peut pas être instancié dans sa
		 * déclaration parce que son constructeur a besoin des variables
		 * d'instance de SetupPermanent, instanciées également lors de leurs
		 * déclarations.
		 * Je ne veux pas devoir gérer des priorités entre variables d'instance.
		 */
		dataMediator = new DataMediator();
		
		listPermanents = createPermanentList(dataMediator);
		updatePermanentList(null);
		
		// Disposer tout ensemble
		JPanel main = createContent();
		
		// Lier la touche ESC à l'action de quitter
		UniversalAction actionQuitter = new UniversalAction(EventHandler.create(
				ActionListener.class, this, "confirmAndQuit"));
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitter");
		main.getActionMap().put("quitter", actionQuitter);
		
		// Cadre principal
		dialog = new JDialog(owner, "Gestion des opérations permanentes");
		dialog.add(main);
		
		// Remplacer la fermeture par l'action personnalisée
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(actionQuitter);
		
		dialog.setPreferredSize(new Dimension(900,600));
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
	
	/**
	 * Crée une liste graphique des contrôleurs d'opérations permanentes.
	 * 
	 * @param dataMediator	Le médiateur de données auquel notifier les
	 * 						changements de sélection.
	 * 
	 * @return				Une nouvelle liste graphique des contrôleurs
	 * 						d'opérations permanentes.
	 */
	private static JList<PermanentController> createPermanentList(
			DataMediator dataMediator) {
		JList<PermanentController> list = new JList<>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(EventHandler.create(
				ListSelectionListener.class, dataMediator, "setController",
				"source.selectedValue"));
		return list;
	}
	
	/**
	 * Renvoie une collection de tous les <code>Permanent</code>, à laquelle est
	 * ajouté un élément <code>null</code> pour permettre la saisie d'une
	 * nouvelle opération.
	 * 
	 * @return	Une nouvelle collection des <code>Permanent</code>s et d'un
	 * 			élément <code>null</code>.
	 * 
	 * @throws IOException
	 * 			En cas d'erreur pendant la récupération des
	 * 			<code>Permanent</code>s.
	 */
	private static Collection<Permanent> getAllPermanentsAndNull()
			throws IOException {
		Collection<Permanent> permanents = new ArrayList<>();
		permanents.add(null);
		permanents.addAll(DAOFactory.getFactory().getPermanentDAO().getAll());
		return permanents;
	}

	/**
	 * Crée un panneau contenant tous les éléments graphiques.
	 */
	private JPanel createContent() {		
		
		// Liste des permanents dans un ScrollPane
		JScrollPane scrollList = new JScrollPane(listPermanents);
		scrollList.setPreferredSize(
				new Dimension(150, scrollList.getPreferredSize().height));
	
		// Panneau des boutons de validation
		JPanel validationPanel = new JPanel();
		validationPanel.setLayout(
				new BoxLayout(validationPanel, BoxLayout.LINE_AXIS));
		
		// Boutons de validation
		JButton reset		= new JButton("Réinitialiser");
		JButton valider		= new JButton("Valider");
		JButton appliquer	= new JButton("Appliquer");
		JButton supprimer	= new JButton("Supprimer");
		JButton quitter		= new JButton("Quitter");
		reset.addActionListener(EventHandler.create(
				ActionListener.class, this, "resetCurrent"));
		valider.addActionListener(EventHandler.create(
				ActionListener.class, this, "applyAllAndQuit"));
		appliquer.addActionListener(EventHandler.create(
				ActionListener.class, this, "applyAllAndGetSelection"));
		supprimer.addActionListener(EventHandler.create(
				ActionListener.class, this, "confirmAndDelete"));
		quitter.addActionListener(EventHandler.create(
				ActionListener.class, this, "confirmAndQuit"));
		validationPanel.add(supprimer);
		validationPanel.add(Box.createHorizontalGlue());	// Pousser à droite
		validationPanel.add(reset);
		validationPanel.add(appliquer);
		validationPanel.add(valider);
		validationPanel.add(quitter);
		// Panneau général
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(										// Créer une marge
				BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(scrollList, BorderLayout.WEST);			// Liste Permanents
		main.add(editionPane);								// Panneau d'édition
		return main;
	}
	
	/**
	 * Crée de nouveaux contrôleurs et les affiche dans la liste graphique.
	 * 
	 * @param selection	Le <code>Permanent</code> à sélectionner après la mise à
	 * 					jour. Si <code>null</code>, sélectionne l'item
	 * 					"Nouveau..."
	 * 
	 * @throws IOException
	 * 					En cas d'erreur pendant la récupération des
	 * 					<code>Permanent</code>s.
	 */
	private void updatePermanentList(Permanent selection) throws IOException {
		
		// Liste des contrôleurs de Permanents
		controllers = new ArrayList<>();
		PermanentController selected = null;
		for (Permanent p : getAllPermanentsAndNull()) {
			PermanentController controller = new PermanentController(p);
			controllers.add(controller);
			if (p == selection) {			// p ET selection peuvent être null
				selected = controller;		// Y compris le contrôleur Nouveau..
			}
		}
		
		// Trier
		Collections.sort(controllers);
		
		// Remplir la liste avec ces contrôleurs
		fillPermanentListFromControllers(selected);
		
		// Les changements intervenus jusqu'ici sont des effets de bord
		for (PermanentController controller : controllers)
			controller.assumeUnmodified();
	}
	
	/**
	 * Remplace le contenu de la liste graphique par les contrôleurs actuels.
	 * 
	 * @param selected	Le contrôleur
	 */
	private void fillPermanentListFromControllers(
			PermanentController selected) {
		DefaultListModel<PermanentController> listModel =
				new DefaultListModel<>();
		for (PermanentController pc : controllers)
			listModel.addElement(pc);
		listPermanents.setModel(listModel);
		listPermanents.setSelectedValue(selected, true);
	}
	
	/**
	 * Réinitialise le contrôleur actuel.
	 */
	public void resetCurrent() {
		dataMediator.getController().reset();
		dataMediator.updateFromController();
	}
	
	/**
	 * Applique toutes les modifications et quitte la boîte de dialogue.
	 * 
	 * @throws IOException
	 * 			En cas d'erreur pendant la récupération des
	 * 			<code>Permanent</code>s.
	 */
	public void applyAllAndQuit() throws IOException {
		applyAllAndGetSelection();
		quit();
	}
	
	/**
	 * Applique toutes les modifications et recharge les données.
	 * 
	 * @throws IOException
	 * 			En cas d'erreur pendant la récupération des
	 * 			<code>Permanent</code>s.
	 */
	public void applyAllAndReload() throws IOException {
		updatePermanentList(applyAllAndGetSelection());
	}
	
	/**
	 * Applique toutes les modifications.
	 * <p>
	 * Cette méthode est appelée dynamiquement par un <code>EventHandler</code>.
	 * Elle doit être publique.
	 * 
	 * @return	Le <code>Permanent</code> à sélectionner après la mise à jour.
	 */
	public Permanent applyAllAndGetSelection() {
		PermanentController selected = dataMediator.getController();
		Permanent selection = selected.getPermanent();
		
		if (!checkValuesConsistency())
			return selection;
		
		for (PermanentController pc : controllers) {

			// Appliquer les modifications
			Permanent p = pc.applyChanges();

			// Actualiser la sélection si besoin
			if (pc == selected) {
				selection = p;
			}
		}
		return selection;
	}
	
	/**
	 * Vérifie que toutes les valeurs sont cohérents pour permettre d'appliquer
	 * les changements.
	 * 
	 * @return	<code>true</code> si les changements peuvent être appliqués sans
	 * 			danger.
	 */
	private boolean checkValuesConsistency() {
		for (PermanentController controller : controllers) {
			String errorMessage = controller.checkErrorMessage();
			
			if (!errorMessage.isEmpty()) {
				int discardChanges = JOptionPane.showConfirmDialog(
						dialog,
						String.format(
								"L'opération %s ne peut pas être enregistrée :%n%s%nVoulez-vous abandonner ses modifications ?",
								controller, errorMessage),
						"Opération incomplète",
						JOptionPane.YES_NO_OPTION);
				
				if (discardChanges == JOptionPane.YES_OPTION) {
					controller.reset();
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Demande conformation à l'utilisateur puis supprime le contrôleur
	 * actuellement sélectionné.
	 */
	public void confirmAndDelete() {
		PermanentController selected = dataMediator.getController();
		
		int confirm = JOptionPane.showConfirmDialog(
				dialog,
				String.format(
						"Voulez-vous vraiment supprimer l'opération permanente %s ?",
						selected),
				"Supprimer une opération permanente",
				JOptionPane.YES_NO_OPTION);
		
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				selected.deletePermanent();
			} catch (DeletionException e1) {
				LOGGER.log(Level.SEVERE,
						"Impossible de supprimer " + selected, e1);
			}
		}
	}
	
	/**
	 * Demande confirmation à l'utilisateur s'il y a des changements non
	 * sauvegardés, puis quitte laboîte de dialogue.
	 */
	public void confirmAndQuit() {
		if (!isModified()) {
			int confirm = JOptionPane.showConfirmDialog(
				dialog,
				"Il y a des changements non enregistrés.\nVoulez-vous les abandonner ?",
				"Abandonner les changements",
				JOptionPane.YES_NO_OPTION);
			
			if (confirm != JOptionPane.YES_OPTION) {
				return;
			}
		}
		quit();
	}
	
	/**
	 * Quitte la boîte de dialogue sans demander confirmation.
	 */
	private void quit() {
		dialog.dispose();
		gui.dataModified();
	}
	
	/**
	 * Indique si l'un des contrôleurs a été modifié depuis la dernière
	 * sauvegarde.
	 */
	private boolean isModified() {
		for (PermanentController controller : controllers) {
			if (controller.isModified()) {
				return true;
			}
		}
		return false;
	}
}