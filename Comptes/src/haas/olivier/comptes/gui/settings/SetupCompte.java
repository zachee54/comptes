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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
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
public class SetupCompte implements ActionListener {
	
	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SetupCompte.class.getName());
	
	/**
	 * Commande permettant d'adapter l'interface pour un compte bancaire.
	 */
	static final String BANCAIRE = "bancaire";
	
	/**
	 * Commande permettant d'adapter l'interface pour un compte budgétaire.
	 */
	static final String BUDGET = "budgétaire";
	
	/**
	 * Commande pour appliquer les changements.
	 */
	private static final String APPLIQUER = "appliquer";
	
	/**
	 * Commande pour valider (appliquer les changements et quitter).
	 */
	static final String VALIDER = "valider";
	
	/**
	 * Commande pour quitter.
	 */
	static final String QUITTER = "quitter";
	
	/**
	 * Commande pour supprimer un compte.
	 */
	static final String SUPPRIMER = "supprimer";
	
	/**
	 * Un médiateur entre les données de l'interface graphique et les données du
	 * modèle.
	 * 
	 * @author Olivier HAAS
	 */
	private class DataMediator implements DocumentListener, ItemListener {
		
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
		 * Le contrôleur de type principal.<br>
		 * C'est celui qui gère les changements de l'interface graphique selon
		 * qu'il s'agit d'un compte bancaire ou budgétaire.
		 */
		private final MainTypeController mainTypeController;
		
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
		 * Construit un médiateur de données écoutant et modifiant les objets
		 * spécifiés.
		 * 
		 * @param mainTypeController	Contrôleur de type principal.
		 * @param nom					Champ de saisie du nom.
		 * @param numero				Champ de saisie du numéro.
		 * @param type					ComboBox de saisie du type secondaire.
		 * @param ouverture				Champ de saisie de la date d'ouverture.
		 * @param cloture				Champ de saisie de la date de clôture.
		 */
		@SuppressWarnings("serial")
		private DataMediator(
				MainTypeController mainTypeController,
				JTextComponent nom,
				JTextComponent numero,
				JButton colorButton,
				JComboBox<TypeCompte> type,
				JTextComponent ouverture,
				JTextComponent cloture) {
			this.mainTypeController = mainTypeController;
			
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
			colorButton.setAction(new AbstractAction(" ") {

				@Override
				public void actionPerformed(ActionEvent e) {
					
					// Laisser choisir la nouvelle couleur
					Color color = JColorChooser.showDialog(
							dialog,
							"Choisissez une couleur",
							controller.getColor());
					
					// Mémoriser
					controller.setColor(color);
					
					// Afficher
					DataMediator.this.colorButton.setBackground(color);
				}
				
			});// classe anonyme AbstractAction
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
			
			// Ignorer la valeur null
			if (controller == null)
				return;
			
			this.controller = controller;
			
			// Cesser d'écouter pour éviter un callback
			nom.getDocument().removeDocumentListener(this);
			numero.getDocument().removeDocumentListener(this);
			ouverture.getDocument().removeDocumentListener(this);
			cloture.getDocument().removeDocumentListener(this);
			type.removeItemListener(this);
			
			// Transcrire les données du nouveau contrôleur
			mainTypeController.changeVue(controller.getMainType());
			nom.setText(controller.getNom());
			numero.setText(controller.getNumero() + "");
			colorButton.setBackground(controller.getColor());
			type.setSelectedItem(controller.getType());
			
			// N'inscrire l'ouverture et la clôture que si elles sont non null
			ouverture.setText(controller.getOuverture() == null
					? ""
					: format.format(controller.getOuverture()));
			cloture.setText(controller.getCloture() == null
					? ""
					: format.format(controller.getCloture()));
			
			// Ré-écouter
			nom.getDocument().addDocumentListener(this);
			numero.getDocument().addDocumentListener(this);
			ouverture.getDocument().addDocumentListener(this);
			cloture.getDocument().addDocumentListener(this);
			type.addItemListener(this);
		}
		
		/**
		 * Reçoit les modifications de type principal, et les renvoie au
		 * contrôleur de type.
		 */
		private void setMainType(String mainType) {
			controller.setMainType(mainType);
		}
		
		/**
		 * Interface <code>ItemListener</code>. Reçoit les notifications de
		 * changement de la ComboBox de type secondaire.
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			Object typeValue = type.getSelectedItem();
			if (typeValue instanceof TypeCompte) {		// Si type de compte
				controller.setType(
						(TypeCompte) typeValue);		// Affecter le type
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
		public void changedUpdate(DocumentEvent e) {}
	}// inner class DataMediator
	
	/**
	 * Un contrôleur gérant les changements de vue entre compte bancaire et
	 * compte budgétaire.
	 * <p>
	 * En particulier, il remplit la combo box des types secondaires en fonction
	 * du type principal.<br>
	 * Il active ou désactive les composants de saisie du numéro en fonction du
	 * type principal.
	 * 
	 * @author Olivier HAAS
	 */
	private class MainTypeController implements ActionListener {
		
		/**
		 * Collection des types bancaires.
		 */
		private TreeSet<TypeCompte> typesBancaire = new TreeSet<>();
		
		/**
		 * Collection des types budgétaires.
		 */
		private TreeSet<TypeCompte> typesBudget = new TreeSet<>();
		
		/**
		 * Bouton radio de choix du type "bancaire".
		 */
		private JRadioButton bancaire;
		
		/**
		 * Bouton radio de choix du type "budgétaire".
		 */
		private JRadioButton budget;
		
		/**
		 * Liste déroulante des types secondaires.
		 */
		private JComboBox<TypeCompte> boxType;
		
		/**
		 * Les composants à activer si c'est un type "compte bancaire", et à
		 * désactiver si c'est un type "compte budgétaire"
		 */
		private Component[] bancaireComponents;
		
		/**
		 * Construit un contrôleur de type mettant à jour les composants
		 * spécifiés.
		 * 
		 * @param bancaire	Bouton radio indiquant le type "compte bancaire".
		 * 
		 * @param budget	Bouton radio indiquant le type "compte budgétaire".
		 * 
		 * @param boxType	La combo box de types secondaires à maintenir à
		 * 					jour.
		 * 
		 * @param bancaireComponents
		 * 					Les composants à activer si c'est un type "compte
		 * 					bancaire", et à désactiver si c'est un type "compte
		 * 					budgétaire".
		 */
		public MainTypeController(JRadioButton bancaire, JRadioButton budget,
				JComboBox<TypeCompte> boxType,
				Component... bancaireComponents) {
			this.bancaire = bancaire;
			this.budget = budget;
			this.boxType = boxType;
			this.bancaireComponents = bancaireComponents;
			
			// Ajouter tous les types dans la bonne collection
			for (TypeCompte type : TypeCompte.values()) {
				if (type.isBudgetaire()) {
					typesBudget.add(type);
				} else if (type.isBancaire()) {
					typesBancaire.add(type);
				}
			}
			
			// Tous
			types = new TreeSet<>(typesBudget);		// Budgétaires
			types.addAll(typesBancaire);			// + bancaires
		}

		/**
		 * Action envoyée par un objet Swing sur action de l'utilisateur.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			changeVue(command);						// Modifier la vue
			dataMediator.setMainType(command);		// Faire suivre au modèle
		}
		
		/**
		 * Modifie la vue en fonction du type principal choisi.
		 * <p>
		 * La méthode re-remplit la combo box de type précis avec les types
		 * correspondant au type principal et active ou désactive les compocants
		 * de saisie du numéro de compte.
		 */
		private void changeVue(String command) {
			boolean enable;
			
			/*
			 * Choisir la collection des types secondaires, sélectionner le bon
			 * bouton radio et déterminer s'il faut activer ou désactiver les
			 * composants.
			 */  
			Set<TypeCompte> typesSet = null;		// La collection
			if (BANCAIRE.equals(command)) {
				typesSet = typesBancaire;			// Coll des types bancaires
				bancaire.setSelected(true);			// Sélectionner bancaire
				enable = true;						// Activer les composants
			} else if (BUDGET.equals(command)) {
				typesSet = typesBudget;				// Coll des types budget
				budget.setSelected(true);			// Sélectionner budgétaire
				enable = false;						// Désactiver les composants
			} else {
				return;								// Pas normal...
			}
			
			// Appliquer l'activation/désactivation des composants
			for (Component component : bancaireComponents) {
				component.setEnabled(enable);
			}
			
			// Vider et re-remplir la liste des types secondaires
			boxType.removeAllItems();				// Vider
			for (TypeCompte type : typesSet) {		// Remplir
				boxType.addItem(type);
			}
		}
	}// inner class MainTypeController

	/**
	 * La boîte de dialogue.
	 */
	private final JDialog dialog;
	
	/**
	 * Le GUI principal.
	 */
	private final SimpleGUI gui;
	
	/**
	 * La liste graphique des comptes.
	 */
	private final JList<CompteController> listComptes;
	
	/**
	 * Le médiateur de données.
	 */
	private final DataMediator dataMediator;
	
	/**
	 * Les contrôleurs de comptes.
	 */
	private ArrayList<CompteController> controllers;
	
	/**
	 * Les types secondaires.
	 */
	private TreeSet<TypeCompte> types;
	
	/**
	 * Construit une boîte de dialogue de gestion des comptes.
	 * 
	 * @param gui	L'instance du GUI en cours.
	 * @param owner	Le cadre auquel rattacher la boîte de dialogue.
	 */
	public SetupCompte(SimpleGUI gui, JFrame owner) {
		this.gui = gui;
		
		// Liste des comptes
		listComptes = createComptesList();
		
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
		
		// Liste déroulante des types
		JComboBox<TypeCompte> boxType = new JComboBox<TypeCompte>();
		
		// Sélection du type principal de compte (bancaire ou budgétaire)
		JRadioButton radioBancaire = new JRadioButton("Compte bancaire");
		JRadioButton radioBudget = new JRadioButton("Compte budgétaire");
		MainTypeController mainTypeController =
				new MainTypeController(radioBancaire, radioBudget, boxType,
						labelNumero, fieldNumero);
		radioBancaire.setActionCommand(BANCAIRE);
		radioBudget.setActionCommand(BUDGET);
		radioBancaire.addActionListener(mainTypeController);
		radioBudget.addActionListener(mainTypeController);
		ButtonGroup groupeClasse = new ButtonGroup();
		groupeClasse.add(radioBancaire);
		groupeClasse.add(radioBudget);
		
		// Boutons de validation
		JButton valider		= new JButton("Valider");		// Bouton valider
		JButton appliquer	= new JButton("Appliquer");		// Bouton appliquer
		JButton quitter		= new JButton("Quitter");		// Bouton quitter
		JButton supprimer	= new JButton("Supprimer");		// Bouton supprimer
		valider		.setActionCommand(VALIDER);				// Commandes
		appliquer	.setActionCommand(APPLIQUER);
		quitter		.setActionCommand(QUITTER);
		supprimer	.setActionCommand(SUPPRIMER);
		valider		.addActionListener(this);				// Listener
		appliquer	.addActionListener(this);
		quitter		.addActionListener(this);
		supprimer	.addActionListener(this);
		
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
				mainTypeController,
				fieldNom,
				fieldNumero,
				colorButton,
				boxType,
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
						.addComponent(boxType))
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
						.addComponent(boxType,
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
		UniversalAction actionQuitter = new UniversalAction(this, QUITTER);
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitter");
		main.getActionMap().put("quitter", actionQuitter);
		
		// Fenêtre principale
		dialog = new JDialog(owner, "Gestion des comptes");
		
		// Remplacer la fermeture de la boîte de dialogue par l'action perso
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(actionQuitter);
		
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
	 * Reçoit les actions des boutons de validation.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		// Mémoriser la sélection actuelle
		CompteController selected =						//Contrôleur sélectionné
				dataMediator.getController();
		Compte selection = selected.getCompte();		// Compte lié
		
		// Appliquer les changements si nécessaire
		if (VALIDER.equals(command) || APPLIQUER.equals(command)) {
			Compte compte;								// Curseur de Compte
			
			// Pour chaque contrôleur
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
		}
		
		// Supprimer si nécessaire
		if (SUPPRIMER.equals(command)
				&& JOptionPane.showConfirmDialog(		// Demander confirmation
						dialog,
						"Voulez-vous vraiment supprimer le compte\n"
						+ selected + " ?",
						"Supprimer un compte",
						JOptionPane.YES_NO_OPTION)
					== JOptionPane.YES_OPTION) {		// Réponse OUI
			try {
				selected.deleteCompte();				// Effacer
				
			} catch (IOException e1) {
				LOGGER.severe("Impossible de supprimer " + selected);
			}
		}
		
		// Quitter ou recharger les données
		if (QUITTER.equals(command) || VALIDER.equals(command)) {
			
			// Vérifier s'il y a des modifications non enregistrées
			boolean modified = false;					// Marqueur de modifs
			for (CompteController controller : controllers) {
				modified = modified || controller.isModified();// Modifié ?
			}
			
			if (!modified								// Pas modifié
					|| JOptionPane.showConfirmDialog(	// Ou confirmé
							dialog,
							"Il y a des changements non enregistrés.\n" +
							"Voulez-vous les abandonner ?",
							"Abandonner les changements",
							JOptionPane.YES_NO_OPTION)
						== JOptionPane.YES_OPTION) {
				dialog.dispose();					// Fermer le dialogue
				gui.createTabs();					// Recréer les onglets
				gui.dataModified();					// Prévenir du changement
				
				// Mettre à jour la liste des comptes dans le TableCellEditor
				FinancialTable.updateComptesEditor();
			}
		} else {
			fillComptesList(selection);				// Recharger les données
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
	private String mainType	= SetupCompte.BANCAIRE;
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
				mainType = SetupCompte.BANCAIRE;
				numero = compte.getNumero();
			} else {
				mainType = SetupCompte.BUDGET;
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
	public String getMainType()	{return mainType;}
	
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
	public void setMainType(String mainType) {
		this.mainType = mainType;
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