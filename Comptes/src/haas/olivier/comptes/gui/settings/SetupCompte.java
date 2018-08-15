package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.SimpleGUI;
import haas.olivier.comptes.gui.table.FinancialTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

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
	 * Un médiateur entre les données de l'interface graphique et les données du
	 * modèle.
	 * 
	 * @author Olivier HAAS
	 */
	public class DataMediator {
		
		/**
		 * Le format de date.
		 */
		private final SimpleDateFormat format =
				new SimpleDateFormat("dd/MM/yyyy");
		
		/**
		 * Le contrôleur de données.<br>
		 * Il gère l'objet <code>Compte</code> à éditer.
		 */
		private CompteController controller = null;
		
		/**
		 * Drapeau indiquant si un changement de contrôleur est en cours. Auquel
		 * cas, les changements de contenu sont dus au basculement de contrôleur
		 * et non à une modification directe par l'utilisateur.
		 */
		private boolean updating = false;
		
		/**
		 * Construit un médiateur de données écoutant et modifiant les champs de
		 * saisie.
		 */
		private DataMediator() {
			
			// La codification EventHandler pour obtenir le document source
			String eventDocument = "document";
			
			nom.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "setNom", eventDocument));
			numero.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "setNumero", eventDocument));
			ouverture.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "setOuverture", eventDocument));
			cloture.getDocument().addDocumentListener(EventHandler.create(
					DocumentListener.class, this, "setCloture", eventDocument));
			typeComboBox.addItemListener(EventHandler.create(ItemListener.class,
					this, "setTypeCompte", "source.selectedItem"));
			colorButton.addActionListener(EventHandler.create(
					ActionListener.class, DataMediator.this, "chooseColor"));
		}
		
		/**
		 * Renvoie le texte contenu dans un champ de saisie.
		 * 
		 * @param document	Le document du champ de saisie.
		 * 
		 * @return			Le texte du document.
		 */
		private String getDocumentText(Document document) {
			try {
				return document.getText(0, document.getLength());
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"Erreur lors de la récupération du texte dans une zone de saisie",
						e);
				return "";
			}
		}
		
		/**
		 * Permet à l'utilisateur de modifier la couleur.
		 */
		public void chooseColor() {
			Color color = JColorChooser.showDialog(dialog,
					"Choisissez une couleur", controller.getColor());
			if (color == null)
				return;
			controller.setColor(color);					// Mémoriser
			colorButton.setBackground(color);			// Afficher
		}
		
		/**
		 * Renvoie le contrôleur vers lequel pointe actuellement l'objet.
		 */
		private CompteController getController() {
			return controller;
		}
	
		/**
		 * Remplace le contrôleur de référence. Les données du nouveau
		 * contrôleur sont retranscrites dans l'interface graphique.
		 */
		public void setController(CompteController controller) {
			if (controller == null)
				return;
			this.controller = controller;
			
			// Transcrire les données du nouveau contrôleur
			updating = true;
			if (controller.getType().isBancaire()) {
				setVueBancaire();
			} else {
				setVueBudget();
			}
			nom.setText(controller.getNom());
			numero.setText(controller.getNumero() + "");
			colorButton.setBackground(controller.getColor());
			typeComboBox.setSelectedItem(controller.getType());
			ouverture.setText(getNotNullDateText(controller.getOuverture()));
			cloture.setText(getNotNullDateText(controller.getCloture()));
			updating = false;
		}
		
		/**
		 * Renvoie la date au format texte, ou une chaîne vide si la date est
		 * <code>null</code>.
		 * 
		 * @param date	La date à formater.
		 * @return		La date au format {@link #format}, ou une chaîne vide si
		 * 				<code>date == null</code>.
		 */
		private String getNotNullDateText(Date date) {
			return (date == null) ? "" : format.format(date);
		}
		
		/**
		 * Modifie le nom du compte dans le contrôleur actuel.
		 * 
		 * @param document	Le document contenant le nouveau nom.
		 */
		public void setNom(Document document) {
			controller.setNom(getDocumentText(document));
		}
		
		/**
		 * Modifie le numéro du compte dans le contrôleur actuel.
		 * 
		 * @param document	Le document contenant le nouveau numéro de compte.
		 */
		public void setNumero(Document document) {
			controller.setNumero(getDocumentText(document));
		}
		
		/**
		 * Modifie le type de compte.
		 * <p>
		 * Si l'interface est en cours de mise à jour, cette méthode ne fait
		 * rien car il ne s'agit pas d'une demande de modification du type du
		 * compte affiché.
		 * 
		 * @param typeCompte	Le nouveau type sélectionné. Toutefois, le
		 * 						contexte ne permet pas de garantir qu'il
		 * 						s'agisse d'une instance <code>TypeCompte</code>.
		 */
		public void setTypeCompte(Object typeCompte) {
			
			// Si l'interface est en cours de mise à jour, ignorer l'événement
			if (updating)
				return;
			
			if (typeCompte instanceof TypeCompte)
				controller.setType((TypeCompte) typeCompte);
		}
		
		/**
		 * Modifie la date d'ouverture du contrôleur actuel.
		 * 
		 * @param document	Le document contenant la date d'ouverture.
		 */
		public void setOuverture(Document document) {
			try {
				controller.setOuverture(
						format.parse(getDocumentText(document)));
			} catch (ParseException e) {
				LOGGER.log(Level.FINEST, "Date d'ouverture illisible", e);
			}
		}
		
		/**
		 * Modifie la date de clôture du contrôleur actuel.
		 * 
		 * @param document	Le document contenant la date de clôture.
		 */
		public void setCloture(Document document) {
			try {
				controller.setCloture(format.parse(getDocumentText(document)));
			} catch (ParseException e) {
				LOGGER.log(Level.FINEST, "Date de clôture illisible", e);
			}
		}
	}// inner class DataMediator	
	
	/**
	 * Action pour quitter.
	 */
	private final ActionListener quitActionListener =
			EventHandler.create(ActionListener.class, this, "quit");
	
	/**
	 * Le GUI principal.
	 */
	private final SimpleGUI gui;

	/**
	 * Zone de saisie du nom.
	 */
	private final JTextComponent nom = new JTextField();
	
	/**
	 * Zone de saisie du numéro de compte bancaire.
	 */
	private final JTextComponent numero = new JTextField();
	
	/**
	 * Zone de saisie de la date d'ouverture.
	 */
	private final JTextComponent ouverture = new JTextField();
	
	/**
	 * Zone de saisie de la date de clôture.
	 */
	private final JTextComponent cloture = new JTextField();
	
	/**
	 * Bouton de choix de la couleur.
	 */
	private final JButton colorButton = new JButton(" ");
	
	/**
	 * La boîte de dialogue.
	 */
	private final JDialog dialog;

	/**
	 * Le médiateur de données.
	 */
	private final DataMediator dataMediator;
	
	/**
	 * Les contrôleurs de comptes.
	 */
	private ArrayList<CompteController> controllers;
	
	/**
	 * La liste graphique des comptes.
	 */
	private final JList<CompteController> listComptes;
	
	/**
	 * Le bouton radio "Compte bancaire".
	 */
	private final JRadioButton radioBancaire =
			new JRadioButton("Compte bancaire");
	
	/**
	 * Le bouton radio "Compte budgétaire".
	 */
	private final JRadioButton radioBudget =
			new JRadioButton("Compte budgétaire");
	
	/**
	 * Liste déroulante des types.
	 */
	private final JComboBox<TypeCompte> typeComboBox = new JComboBox<>();
	
	/**
	 * Les composants à activer uniquement pour les comptes de type bancaire.
	 */
	private final Collection<Component> bancaireComponents = new ArrayList<>(2);
	
	/**
	 * Construit une boîte de dialogue de gestion des comptes.
	 * 
	 * @param gui	L'instance du GUI en cours.
	 * @param owner	Le cadre auquel rattacher la boîte de dialogue.
	 */
	public SetupCompte(SimpleGUI gui, JFrame owner) {
		this.gui = gui;
		dataMediator = new DataMediator();
		listComptes = createComptesList();
		fillComptesList(null);
		configureTypeRadioButtons();
		UniversalAction actionQuitter = new UniversalAction(quitActionListener);
		
		// Panneau principal
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(createListComptesPanel(), BorderLayout.WEST);
		main.add(createEditionPanel());
		main.add(createValidationPanel(), BorderLayout.SOUTH);
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitter");
		main.getActionMap().put("quitter", actionQuitter);
		
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
	 * Crée une liste de comptes et écoute les changements de sélection.
	 * <p>
	 * Attention : {@link #dataMediator} doit être préalablement instancié.
	 * 
	 * @param dataMediator	Le médiateur de données auquel notifier les
	 * 						changements de sélection.
	 * 
	 * @return				Une nouvelle liste graphique des comptes.
	 */
	private JList<CompteController> createComptesList() {
		JList<CompteController> list = new JList<>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(EventHandler.create(
				ListSelectionListener.class, dataMediator, "setController",
				"source.selectedValue"));
		return list;
	}

	/**
	 * Re-remplit la liste graphique des comptes.
	 * 
	 * @param selection	Le compte à sélectionner après la mise à jour. Utiliser
	 * 					<code>null</code> pour un nouveau compte.
	 */
	private void fillComptesList(Compte selection) {
		
		// Tout insérer dans le modèle
		DefaultListModel<CompteController> listModel = new DefaultListModel<>();
		for (CompteController controller : createControllers(selection))
			listModel.addElement(controller);
		listComptes.setModel(listModel);
		
		// Sélectionner le bon item
		listComptes.setSelectedValue(dataMediator.getController(), true);
	}

	/**
	 * Crée les contrôleurs de comptes.
	 * 
	 * @param selection		Le compte à sélectionner, ou <code>null</code> pour
	 * 						sélectionner le contrôleur qui permet de définir un
	 * 						nouveau compte.
	 * 
	 * @param dataMediator	Le médiateur de données auquel notifier le
	 * 						contrôleur du compte sélectionné.
	 * 
	 * @return				Les contrôleurs des comptes, triés selon leur ordre
	 * 						naturel.
	 */
	private Iterable<CompteController> createControllers(Compte selection) {
		Collection<Compte> comptes = new ArrayList<>(getAllComptes());
		
		// Ajouter un compte null qui servira pour le contrôleur "Nouveau..."
		comptes.add(null);
		
		controllers = new ArrayList<>();
		for (Compte compte : comptes) {
			CompteController controller = new CompteController(compte); 
			controllers.add(controller);
			if (compte == selection) {					// Éventuellement null
				dataMediator.setController(controller);
			}
		}
		Collections.sort(controllers);
		return controllers;
	}

	/**
	 * Configure le comportement des boutons radio "Compte bancaire" et
	 * "Compte budgétaire".
	 */
	private void configureTypeRadioButtons() {
		ButtonGroup groupeClasse = new ButtonGroup();
		groupeClasse.add(radioBancaire);
		groupeClasse.add(radioBudget);
		radioBancaire.addActionListener(EventHandler.create(
				ActionListener.class, this, "setVueBancaire"));
		radioBudget.addActionListener(EventHandler.create(
				ActionListener.class, this, "setVueBudget"));
	}
	
	/**
	 * Crée un panneau défilable contenant la liste des comptes.
	 * 
	 * @return	Un nouveau panneau défilable contenant la liste des comptes.
	 */
	private JComponent createListComptesPanel() {
		JScrollPane scrollList = new JScrollPane(listComptes);
		scrollList.setPreferredSize(					// Largeur préférée
				new Dimension(150, scrollList.getPreferredSize().height));
		return scrollList;
	}

	/**
	 * Crée le panneau d'édition contenant les champs de saisie pour un compte.
	 * 
	 * @return	Un panneau contenant tous les champs de saisie pour un compte.
	 */
	private JComponent createEditionPanel() {
		
		// Champs modifiables
		JLabel labelNature		= new JLabel("Nature :");
		JLabel labelCouleur		= new JLabel("Couleur :");
		JLabel labelNom			= new JLabel("Nom :");
		JLabel labelOuverture	= new JLabel("Ouverture :");
		JLabel labelCloture		= new JLabel("Clôture :");
		JLabel labelNumero		= new JLabel("Numéro :");
		JLabel labelType		= new JLabel("Type :");
		
		// Composants à n'activer que pour les comptes bancaires
		bancaireComponents.add(labelNumero);
		bancaireComponents.add(numero);
		
		// Panneau de sélection du type principal (bancaire ou budgétaire)
		JPanel hautDroite = new JPanel();
		hautDroite.setLayout(new BoxLayout(hautDroite, BoxLayout.PAGE_AXIS));
		hautDroite.setBorder(BorderFactory.createTitledBorder((Border) null));
		hautDroite.add(radioBancaire);
		hautDroite.add(radioBudget);
		
		// Panneau de couleur pour les diagrammes
		JPanel couleurPanel = new JPanel(new BorderLayout());
		couleurPanel.add(colorButton);

		// Assembler tout
		JPanel editionPanel = new JPanel();
		GroupLayout layout = new GroupLayout(editionPanel);
		editionPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);					// Espaces automatiques
		layout.setAutoCreateContainerGaps(true);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.CENTER)
						.addComponent(labelNature)
						.addComponent(hautDroite))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.CENTER, false)
						.addComponent(labelCouleur)
						.addComponent(couleurPanel))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelNom)
						.addComponent(nom))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelType)
						.addComponent(typeComboBox))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelOuverture)
						.addComponent(ouverture))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelCloture)
						.addComponent(cloture))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelNumero)
						.addComponent(numero)));
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.TRAILING)
						.addComponent(labelNature)
						.addComponent(labelCouleur)
						.addComponent(labelNom)
						.addComponent(labelType)
						.addComponent(labelOuverture)
						.addComponent(labelCloture)
						.addComponent(labelNumero))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.LEADING)
						.addComponent(hautDroite)
						.addComponent(couleurPanel)
						.addComponent(nom,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(typeComboBox,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(ouverture,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(cloture,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(numero,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)));
		return editionPanel;
	}
	
	/**
	 * Crée un panneau horizontal contenant les boutons d'action.
	 * 
	 * @return	Le composant graphique contenant tous les boutons.
	 */
	private JComponent createValidationPanel() {
		
		// Les boutons
		JButton valider		= new JButton("Valider");
		JButton appliquer	= new JButton("Appliquer");
		JButton supprimer	= new JButton("Supprimer");
		JButton quitter		= new JButton("Quitter");
		valider.addActionListener(
				EventHandler.create(ActionListener.class, this, "validate"));
		appliquer.addActionListener(
				EventHandler.create(ActionListener.class, this, "apply"));
		supprimer.addActionListener(
				EventHandler.create(ActionListener.class, this,
						"confirmDeletion"));
		quitter.addActionListener(quitActionListener);

		// Barre contenant les boutons
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bar.add(supprimer);
		bar.add(Box.createHorizontalGlue());
		bar.add(appliquer);
		bar.add(valider);
		bar.add(quitter);
		return bar;
	}
	
	/**
	 * Adapte la vue pour les comptes bancaires.
	 * <p>
	 * Le bouton radio "Compte bancaire" est sélectionné.
	 * Les composants spécifiques aux comptes bancaires (en l'occurrence le
	 * champ "Numéro" et son étiquette) sont activés.<br>
	 * La liste déroulante des types propose les différents types bancaires.
	 */
	// TODO L'ancienne implémentation prévoyait de notifier un changement de type principal (mais pas de TypeCompte) à CompteController. Non repris car apparemment inutile, mais à vérifier.
	public void setVueBancaire() {
		radioBancaire.setSelected(true);
		setVueMainType(true);
	}
	
	/**
	 * Adapte la vue pour les comptes budgétaires.
	 * <p>
	 * Le bouton radio "Compte budgétaire" est sélectionné.
	 * Les composants spécifiques aux comptes bancaires (en l'occurrence le
	 * champ "Numéro" et son étiquette) sont désactivés.<br>
	 * La liste déroulante des types propose les différents types budgétaires.
	 */
	public void setVueBudget() {
		radioBudget.setSelected(true);
		setVueMainType(false);
	}
	
	/**
	 * Adapte la vue, selon le cas, pour les comptes bancaires ou pour les
	 * comptes budgétaires.
	 * <p>
	 * Les composants spécifiques aux comptes bancaires (en l'occurrence le
	 * champ Numéro" et son étiquette) sont activés ou désactivés.<br>
	 * La liste déroulante des types est modifiée pour ne contenir que les types
	 * bancaires, ou que les types budgétaires.
	 * 
	 * @param bancaire	<code>true</code> pour adapter la vue pour les comptes
	 * 					bancaires, <code>false</code> pour adapter la vue pour
	 * 					les comptes budgétaires.
	 */
	private void setVueMainType(boolean bancaire) {
		for (Component component : bancaireComponents)
			component.setEnabled(bancaire);
		
		typeComboBox.removeAll();
		for (TypeCompte typeCompte : TypeCompte.values()) {
			if (typeCompte.isBancaire() == bancaire)
				typeComboBox.addItem(typeCompte);
		}
	}
	
	/**
	 * Valide l'ensemble des modifications.
	 */
	public void apply() {
		
		// Mémoriser la sélection actuelle
		CompteController selected = dataMediator.getController();
		Compte selection = selected.getCompte();
		
		// Appliquer toutes les modifications
		Month toUpdate = null;
		for (CompteController controller : controllers) {
			if (!controller.isModified())
				continue;
			
			// Dates du compte avant les changements
			Compte compte = controller.getCompte();
			Date oldOuverture = null;
			Date oldCloture = null;
			if (compte != null) {
				oldOuverture = compte.getOuverture();
				oldCloture = compte.getCloture();
			}
			
			// Nouveau compte (même instance qu'avant, sauf s'il était null)
			compte = controller.applyChanges();

			// Noter de mettre à jour les bases en cas de changement
			toUpdate = getOlderMonth(toUpdate, oldOuverture);
			toUpdate = getOlderMonth(toUpdate, oldCloture);
			toUpdate = getOlderMonth(toUpdate, compte.getOuverture());
			toUpdate = getOlderMonth(toUpdate, compte.getCloture());

			// Actualiser la sélection si besoin
			if (controller == selected) {
				selection = compte;
			}
		}

		// Mettre à jour les suivis
		if (toUpdate != null) {
			try {
				EcritureController.updateSuivis(toUpdate);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Échec de mise à jour des suivis", e);
			}
		}
		
		// Recharger la (nouvelle) liste des comptes
		fillComptesList(selection);
	}
	
	/**
	 * Renvoie le mois le plus ancien : soit le mois spécifié, soit le mois de
	 * la date spécifiée.
	 * 
	 * @param month	Un mois. Peut être <code>null</code>.
	 * @param date	Une date. Peut être <code>null</code>.
	 * 
	 * @return		<code>null</code> si <code>month/code> et <code>date</code>
	 * 				sont <code>null</code> ; si l'un des deux est
	 * 				<code>null</code>, alors le mois de l'autre ; si
	 * 				<code>month</code> est plus ancien que <code>date</code>,
	 * 				alors <code>month</code> ; sinon, le mois de
	 * 				<code>date</code>.
	 */
	private Month getOlderMonth(Month month, Date date) {
		if (month == null) {
			return (date == null) ? null : new Month(date);
		} else if (date == null) {
			return month;
		} else {
			return month.before(date) ? month : new Month(date);
		}
	}
	
	/**
	 * Applique l'ensemble des modifications et ferme la boîte de dialogue.
	 */
	public void validate() {
		apply();
		quit();
	}
	
	/**
	 * Ferme la boîte de dialogue.<br>
	 * S'il y a des changements non enregistrés, l'utilisateur est invité à
	 * confirmer l'action.
	 */
	public void quit() {
		
		// Vérifier s'il y a des modifications non enregistrées
		if (isModified() && !confirm())
			return;
		
		dialog.dispose();							// Fermer le dialogue
		gui.createTabs();							// Recréer les onglets
		gui.dataModified();							// Prévenir du changement

		// Mettre à jour la liste des comptes dans le TableCellEditor
		FinancialTable.updateComptesEditor();
	}
	
	/**
	 * Indique s'il existe des changements non validés.
	 * 
	 * @return	<code>true</code> s'il existe des changements non validés.
	 */
	private boolean isModified() {
		for (CompteController controller : controllers) {
			if (controller.isModified()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Affiche une boîte de dialogue pour demander à l'utilisateur q'il souhaite
	 * quitter malgré les changements non validés.
	 * 
	 * @return	<code>true</code> si l'utilisateur confirme.
	 */
	private boolean confirm() {
		return JOptionPane.showConfirmDialog(
				dialog,
				"Il y a des changements non enregistrés.\nVoulez-vous les abandonner ?",
				"Abandonner les changements",
				JOptionPane.YES_NO_OPTION)
			== JOptionPane.YES_OPTION;
	}

	/**
	 * Demande confirmation à l'utilisateur avant de supprimer un compte. Si
	 * l'utilisateur confirme, le compte est effectivement supprimé.
	 */
	public void confirmDeletion() {
		CompteController selected = dataMediator.getController();
		int confirm = JOptionPane.showConfirmDialog(
				dialog,
				String.format("Voulez-vous vraiment supprimer le compte%n%s ?",
						selected),
				"Supprimer un compte",
				JOptionPane.YES_NO_OPTION);
		
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				selected.deleteCompte();
			} catch (IOException e1) {
				LOGGER.severe("Impossible de supprimer " + selected);
			}
		}
	}
}