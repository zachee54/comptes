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
import java.awt.event.ItemEvent;
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
import java.util.HashSet;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
	 * Action pour quitter.
	 */
	final ActionListener quitActionListener =
			EventHandler.create(ActionListener.class, this, "quit");
	
	/**
	 * Un médiateur entre les données de l'interface graphique et les données du
	 * modèle.
	 * 
	 * @author Olivier HAAS
	 */
	public class DataMediator implements DocumentListener, ItemListener {
		
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
		 * Zone de saisie du nom.
		 */
		private final JTextComponent nom;
		
		/**
		 * Zone de saisie du numéro de compte bancaire.
		 */
		private final JTextComponent numero;
		
		/**
		 * Zone de saisie de la date d'ouverture.
		 */
		private final JTextComponent ouverture;
		
		/**
		 * Zone de saisie de la date de clôture.
		 */
		private final JTextComponent cloture;
		
		/**
		 * Bouton de choix de la couleur.
		 */
		private final JButton colorButton;
		
		/**
		 * Liste déroulante pour choisir le type secondaire.
		 */
		private final JComboBox<TypeCompte> type;
		
		/**
		 * Drapeau indiquant si un changement de contrôleur est en cours. Auquel
		 * cas, les changements de contenu sont dus au basculement de contrôleur
		 * et non à une modification directe par l'utilisateur.
		 */
		private boolean updating = false;
		
		/**
		 * Construit un médiateur de données écoutant et modifiant les objets
		 * spécifiés.
		 * 
		 * @param nom		Champ de saisie du nom.
		 * @param numero	Champ de saisie du numéro.
		 * @param type		ComboBox de saisie du type secondaire.
		 * @param ouverture	Champ de saisie de la date d'ouverture.
		 * @param cloture	Champ de saisie de la date de clôture.
		 */
		private DataMediator(JTextComponent nom, JTextComponent numero,
				JButton colorButton, JComboBox<TypeCompte> type,
				JTextComponent ouverture, JTextComponent cloture) {
			
			// Mémoriser et écouter les champs de saisie
			this.nom = nom;
			nom.getDocument().addDocumentListener(this);
			
			this.numero = numero;
			numero.getDocument().addDocumentListener(this);
			
			this.ouverture = ouverture;
			ouverture.getDocument().addDocumentListener(this);
			
			this.cloture = cloture;
			cloture.getDocument().addDocumentListener(this);
			
			this.type = type;
			type.addItemListener(this);
			
			// Mémoriser le bouton couleur et définir l'action
			this.colorButton = colorButton;
			colorButton.addActionListener(EventHandler.create(
					ActionListener.class, DataMediator.this, "chooseColor"));
		}
		
		/**
		 * Permet à l'utilisateur de modifier la couleur.
		 */
		public void chooseColor() {
			Color color = JColorChooser.showDialog(
					dialog,
					"Choisissez une couleur",
					controller.getColor());
			if (color == null)
				return;
			
			// Mémoriser
			controller.setColor(color);
			
			// Afficher
			DataMediator.this.colorButton.setBackground(color);
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
		private void setController(CompteController controller) {
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
			type.setSelectedItem(controller.getType());
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
		 * Interface <code>ItemListener</code>. Reçoit les notifications de
		 * changement de la ComboBox de type secondaire.
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			
			// Si l'interface est en cours de mise à jour, ignorer l'événement
			if (updating)
				return;
			
			Object typeValue = type.getSelectedItem();
			if (typeValue instanceof TypeCompte) {
				controller.setType((TypeCompte) typeValue);
			}
		}
		
		/**
		 * Reçoit les modifications des zones de texte dans l'interface et les
		 * envoie vers le modèle.
		 * 
		 * @param e	Le <code>DocumentEvent</code> généré par le
		 * 			<code>JTextComponent</code> modifié.
		 */
		private void textChanged(DocumentEvent e) {
			try {
				Document doc = e.getDocument();
				if (doc == nom.getDocument()) {
					controller.setNom(nom.getText());			// Nom
				} else if (doc == numero.getDocument()) {
					controller.setNumero(
							Long.parseLong(numero.getText()));	// Numéro
				} else if (doc == ouverture.getDocument()) {
					controller.setOuverture(
							format.parse(ouverture.getText()));	// Ouverture
				} else if (doc == cloture.getDocument()) {
					controller.setCloture(
							format.parse(cloture.getText()));	// Clôture
				}
			} catch (ParseException e1) {		// Numéro illisible:rien à faire
			} catch (NumberFormatException e2) {// Date illisible  :rien à faire
			}
		}
		
		/**
		 * Interface <code>DocumentListener</code>. Reçoit les notifications de
		 * changement sur le nom.
		 */
		@Override
		public void insertUpdate(DocumentEvent e) {
			textChanged(e);
		}

		/**
		 * Interface <code>DocumentListener</code>. Reçoit les notifications de
		 * changement sur le nom.
		 */
		@Override
		public void removeUpdate(DocumentEvent e) {
			textChanged(e);
		}

		/**
		 * Interface <code>DocumentListener</code>. Aucune implémentation.
		 */
		@Override
		public void changedUpdate(DocumentEvent e) {
			/* Seuls les changements de texte ont un intérêt */
		}
	}// inner class DataMediator
	
	/**
	 * La boîte de dialogue.
	 */
	private final JDialog dialog;
	
	/**
	 * Le GUI principal.
	 */
	private final SimpleGUI gui;
	
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
	private final Component[] bancairesComponents;
	
	/**
	 * Construit une boîte de dialogue de gestion des comptes.
	 * 
	 * @param gui	L'instance du GUI en cours.
	 * @param owner	Le cadre auquel rattacher la boîte de dialogue.
	 */
	public SetupCompte(SimpleGUI gui, JFrame owner) {
		this.gui = gui;
		
		// Champs modifiables
		JLabel labelNature			= new JLabel("Nature :");
		JLabel labelCouleur			= new JLabel("Couleur :");
		JLabel labelNom				= new JLabel("Nom :");
		JTextField fieldNom			= new JTextField();
		JLabel labelOuverture		= new JLabel("Ouverture :");
		JTextField fieldOuverture	= new JTextField();
		JLabel labelCloture			= new JLabel("Clôture :");
		JTextField fieldCloture		= new JTextField();
		JLabel labelNumero			= new JLabel("Numéro :");
		JTextField fieldNumero		= new JTextField();
		JLabel labelType			= new JLabel("Type :");
		
		// Liste des comptes
		listComptes = createComptesList();
		
		// Composants à n'activer que pour les comptes de type bancaire
		bancairesComponents = new Component[] {labelNumero, fieldNumero};
		
		// Sélection du type principal de compte (bancaire ou budgétaire)
		prepareTypeRadioButtons();
		
		// Boutons de validation
		JButton valider		= new JButton("Valider");		// Bouton valider
		JButton appliquer	= new JButton("Appliquer");		// Bouton appliquer
		JButton quitter		= new JButton("Quitter");		// Bouton quitter
		quitter.addActionListener(quitActionListener);
		valider.addActionListener(
				EventHandler.create(ActionListener.class, this, "validate"));
		appliquer.addActionListener(
				EventHandler.create(ActionListener.class, this, "apply"));
		
		JButton supprimer = new JButton("Supprimer");
		supprimer.addActionListener(EventHandler.create(
				ActionListener.class, this, "confirmDeletion"));
		
		// Agencement général
		
		// Panneau de sélection du type principal (bancaire ou budgétaire)
		JPanel hautDroite = new JPanel();				// Panneau
		hautDroite.setLayout(							// Agencement vertical
				new BoxLayout(hautDroite, BoxLayout.PAGE_AXIS));
		hautDroite.setBorder(							// Bordure avec titre
				BorderFactory.createTitledBorder((Border) null));
		hautDroite.add(radioBancaire);					// Boutons radio
		hautDroite.add(radioBudget);
		
		// Panneau de couleur pour les diagrammes
		JPanel couleurPanel = new JPanel(				// Panneau
				new BorderLayout());
		JButton colorButton = new JButton();			// Bouton couleur
		couleurPanel.add(colorButton);					// Ajouter le bouton
		
		// Médiateur de données
		dataMediator = new DataMediator(
				fieldNom,
				fieldNumero,
				colorButton,
				typeComboBox,
				fieldOuverture,
				fieldCloture);
		
		// Remplir la liste des comptes en sélectionnant "Nouveau..."
		fillComptesList(null);

		// Corps de la fenêtre
		JPanel corps = new JPanel();					// Le panneau
		GroupLayout layout = new GroupLayout(corps);	// Agencement en groupes
		corps.setLayout(layout);
		layout.setAutoCreateGaps(true);					// Espaces automatiques
		layout.setAutoCreateContainerGaps(true);
		layout.setVerticalGroup(						// Insertion verticals
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
						.addComponent(fieldNom))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelType)
						.addComponent(typeComboBox))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelOuverture)
						.addComponent(fieldOuverture))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelCloture)
						.addComponent(fieldCloture))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelNumero)
						.addComponent(fieldNumero)));
		layout.setHorizontalGroup(						// Insertion horizontale
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.TRAILING)
						.addComponent(labelNature)
						.addComponent(labelCouleur)
						.addComponent(labelNom)
						.addComponent(labelType)
						// Écouter la sélection.addComponent(labelOuverture)
						.addComponent(labelCloture)
						.addComponent(labelNumero))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.LEADING)
						.addComponent(hautDroite)
						.addComponent(couleurPanel)
						.addComponent(fieldNom,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(typeComboBox,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldOuverture,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldCloture,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldNumero,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)));

		// Partie basse
		JPanel bas = new JPanel();						// Le panneau
		bas.setLayout(
				new BoxLayout(bas, BoxLayout.X_AXIS));	// Agencement horizontal
		bas.setBorder(									// Bordure 10px en haut
				BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bas.add(supprimer);								// Bouton supprimer
		bas.add(Box.createHorizontalGlue());			// Glue à gauche
		bas.add(appliquer);								// Bouton appliquer
		bas.add(valider);								// Bouton valider
//		bas.add(Box.createRigidArea(					// Un peu de place
//				new Dimension(10,0)));
		bas.add(quitter);								// Bouton quitter

		// Panneau défilable de liste des comptes
		JScrollPane scrollList =						// ScrollPane
				new JScrollPane(listComptes);
		scrollList.setPreferredSize(					// Dimensions
				new Dimension(150, scrollList.getPreferredSize().height));

		// Panneau principal
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(									// Marge extérieure
				BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(scrollList, BorderLayout.WEST);		// Liste des comptes
		main.add(corps);								// Corps de la fenêtre
		main.add(bas, BorderLayout.SOUTH);				// Boutons de validation
		
		// Lier la touche Echap à l'action de quitter
		UniversalAction actionQuitter = new UniversalAction(quitActionListener);
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitter");
		main.getActionMap().put("quitter", actionQuitter);
		
		// Fenêtre principale
		dialog = new JDialog(owner, "Gestion des comptes");
		
		dialog.add(main);
		dialog.pack();
		dialog.setLocationRelativeTo(null);			// Centrer
		dialog.setVisible(true);
	}// constructeur
	
	/**
	 * Crée une liste de comptes dont les changements sont écoutés.
	 * 
	 * @return	Une nouvelle liste graphique des comptes.
	 */
	@SuppressWarnings("unchecked")
	private JList<CompteController> createComptesList() {
		JList<CompteController> list = new JList<>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(
				e -> dataMediator.setController(
						((JList<CompteController>) e.getSource())
						.getSelectedValue()));
		return list;
	}
	
	/**
	 * Configure le comportement des boutons radio "Compte bancaire" et
	 * "Compte budgétaire".
	 */
	private void prepareTypeRadioButtons() {
		ButtonGroup groupeClasse = new ButtonGroup();
		groupeClasse.add(radioBancaire);
		groupeClasse.add(radioBudget);
		radioBancaire.addActionListener(EventHandler.create(
				ActionListener.class, this, "setVueBancaire"));
		radioBudget.addActionListener(EventHandler.create(
				ActionListener.class, this, "setVueBudget"));
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
		for (Component component : bancairesComponents)
			component.setEnabled(bancaire);
		
		typeComboBox.removeAll();
		for (TypeCompte typeCompte : TypeCompte.values()) {
			if (typeCompte.isBancaire() == bancaire)
				typeComboBox.addItem(typeCompte);
		}
	}
	
	
	/**
	 * Re-remplit la liste graphique des comptes.
	 * 
	 * @param selection	Le compte à sélectionner après la mise à jour. Utiliser
	 * 					<code>null</code> pour un nouveau compte.
	 */
	private void fillComptesList(Compte selection) {
		
		// Obtenir la liste de tous les comptes
		Collection<Compte> comptes;
		try {
			comptes = DAOFactory.getFactory().getCompteDAO().getAll();
		} catch (IOException e) {
			comptes = new HashSet<Compte>();
			e.printStackTrace();
		}
		
		// Ajouter un null pour une nouvelle saisie
		comptes.add(null);
		
		// Liste de contrôleurs de comptes
		controllers = new ArrayList<CompteController>();// Créer la liste 
		CompteController selected = null;				// Curseur de contrôleur
		for (Compte compte : comptes) {
			CompteController controller =				// Nouveau contrôleur
					new CompteController(compte); 
			controllers.add(controller);				// Insérer le contrôleur
			if (compte == selection) {
				selected = controller;					// Repérer la sélection
			}
		}
		Collections.sort(controllers);					// Trier les contrôleurs
		
		// Tout insérer dans le modèle
		DefaultListModel<CompteController> listModel =
				new DefaultListModel<CompteController>();
		for (CompteController cc : controllers) {
			listModel.addElement(cc);						// Remplir modèle
		}
		listComptes.setModel(listModel);					// Affecter modèle
		
		// Sélectionner le bon item
		listComptes.setSelectedValue(selected, true);
	}

	/**
	 * Valide l'ensemble des modifications.
	 */
	public void apply() {
		
		// Mémoriser la sélection actuelle
		CompteController selected =						//Contrôleur sélectionné
				dataMediator.getController();
		Compte selection = selected.getCompte();		// Compte lié
		
			
		// Pour chaque contrôleur
		Compte compte;								// Curseur de Compte
		Month toUpdate = null;
		for (CompteController controller : controllers) {
			try {
				Compte oldCompte =
						controller.getCompte();		// Ancien compte

				// Appliquer les modifs
				compte = controller.applyChanges();

				// Noter de mettre à jour les bases en cas de changement
				if (oldCompte != null && compte != oldCompte) {

					// Comparer la date de mise à jour et l'ancien compte
					if (toUpdate == null
							|| toUpdate.before(oldCompte.getOuverture())) {
						// L'ancien compte est plus vieux: reculer la date
						toUpdate = new Month(oldCompte.getOuverture());
					}

					// Comparer aussi le nouveau compte
					if (toUpdate.before(compte.getOuverture())) {
						// Le nouveau compte remonte plus loin: reculer
						toUpdate = new Month(compte.getOuverture());
					}
				}

			} catch (IOException e1) {
				LOGGER.severe("Impossible de sauvegarder le compte "
						+ controller);

				// Arrêter ici sans recharger les données
				return;
			}

			// Actualiser la sélection si besoin
			if (controller == selected) {
				selection = compte;
			}
		}

		// Appliquer une mise à jour
		if (toUpdate != null) {
			try {
				EcritureController.updateSuivis(toUpdate);
			} catch (IOException e1) {
				LOGGER.log(
						Level.WARNING,
						"Échec de mise à jour des suivis",
						e1);
			}
		}
		
		fillComptesList(selection);				// Recharger les données
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
				String.format("Voulez-vous vraiment supprimer le compte\n%s ?",
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
}// class SetupCompte

/**
 * Un contrôleur de Compte.<br>
 * Il permet de pré-visualiser les changements à apporter à un compte.
 * <p>
 * Cette classe est externe à <code>SetupCompte</code> pour éviter que celle-ci
 * n'accède aux propriétés privées du contrôleur, au lieu de passer par les
 * setters.
 */
class CompteController implements Comparable<CompteController> {
	
	/**
	 * Le compte contrôlé.
	 */
	private Compte compte = null;
	
	/**
	 * Marqueur de modifications.
	 */
	private boolean modified = false;
	
	/**
	 * Propriété à éditer.<br>
	 * La valeur par défaut est celle à utiliser pour une nouvelle saisie.
	 */
	private String nom		= null;
	private Color color		= null;
	private TypeCompte type	= TypeCompte.COMPTE_COURANT;
	private long numero		= 0L;
	private Date ouverture	= null;
	private Date cloture	= null;
	
	/**
	 * Construit un contrôleur contenant les données actuelles du compte
	 * spécifié.
	 * 
	 * @param compte	Le compte à éditer. Utiliser <code>null</code> pour un
	 * 					nouveau compte.
	 */
	public CompteController(Compte compte) {
		this.compte = compte;
		refresh();
	}
	
	/**
	 * Rafraîchit les données à partir du compte actuel.
	 */
	private void refresh() {
		if (compte != null) {
			
			// Récupérer les propriétés classiques
			nom = compte.getNom();
			color = compte.getColor();
			type = compte.getType();
			ouverture = compte.getOuverture();
			cloture = compte.getCloture();
			
			// Selon le type du compte (bancaire ou budgétaire)
			TypeCompte type = compte.getType();
			if (type.isBancaire()) {
				numero = compte.getNumero();
			}
		}
	}
	
	/**
	 * Renvoie le compte actuel.
	 */
	public Compte getCompte() {
		return compte;
	}
	
	// Getters
	public String getNom()		{return nom;}
	public Color getColor()		{return color;}
	public TypeCompte getType()	{return type;}
	public long getNumero()		{return numero;}
	public Date getOuverture()	{return ouverture;}
	public Date getCloture()	{return cloture;}
	
	// Setters
	public void setNom(String nom) {
		this.nom = nom;
		modified = true;
	}
	public void setColor(Color color) {
		this.color = color;
		modified = true;
	}
	public void setType(TypeCompte type) {
		this.type = type;
		modified = true;
	}
	public void setNumero(long numero) {
		this.numero = numero;
		modified = true;
	}
	public void setOuverture(Date ouverture) {
		this.ouverture = ouverture;
		modified = true;
	}
	public void setCloture(Date cloture) {
		this.cloture = cloture;
		modified = true;
	}
	
	/**
	 * Indique si les données ont été modifiées par rapport à l'état d'origine
	 * du compte.
	 */
	public boolean isModified() {
		return modified;
	}
	
	/**
	 * Applique les changements au compte et envoie la nouvelle instance au DAO.
	 * 
	 * @return	Le nouveau compte enregistré dans le DAO. S'il n'y a pas de
	 * 			modifications, c'est la même instance qu'auparavant.
	 * 
	 * @throws IOException
	 */
	public Compte applyChanges() throws IOException {
		if (!modified)								// Pas de modifications
			return compte;							// Ne rien faire
		
		// Instancier un nouveau Compte si besoin
		if (compte == null) {
			compte = new Compte(type);
			DAOFactory.getFactory().getCompteDAO().add(compte);
		}
		
		// Ajuster les nouvelles propriétés
		compte.setColor(color);
		compte.setOuverture(ouverture);
		compte.setCloture(cloture);
		
		// Réinitialiser le marqueur de modifications
		modified = false;
		
		// Rafraîchir les données
		refresh();
		
		return compte;
	}
	
	/**
	 * Supprime le compte.
	 * 
	 * @throws IOException
	 */
	public void deleteCompte() throws IOException {
		DAOFactory.getFactory().getCompteDAO().remove(compte);
	}

	/**
	 * Applique la relation d'ordre des comptes aux contrôleurs de comptes.<br>
	 * Si un contrôleur et vide ("Nouveau..."), il passe avant.
	 */
	@Override
	public int compareTo(CompteController controller) {
		Compte compte2 = controller.getCompte();
		if (compte != null && compte2 != null) {	// Cas général
			return compte.compareTo(compte2);
		} else if (compte == null && compte2 != null) {
			return -1;								// Le null en premier
		} else if (compte != null && compte2 == null) {
			return 1;								// Le non-null en dernier
		} else {
			return 0;								// Deux null: égalité !
		}
	}
	
	/**
	 * Renvoie le nom du compte vers lequel pointe l'objet.
	 */
	@Override
	public String toString() {
		if (compte == null) {
			return "Nouveau...";
		} else {
			return compte.toString();
		}
	}
}