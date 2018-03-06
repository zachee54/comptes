package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.gui.SimpleGUI;
import haas.olivier.comptes.gui.table.ComptesComboBoxRenderer;
import haas.olivier.comptes.gui.table.FinancialTable;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

/** Une boîte de dialogue pour paramétrer les Permanents. 
 * 
 * @author Olivier HAAS.
 */
public class SetupPermanent implements ActionListener, ListSelectionListener {
	
	/** Le Logger de cette classe. */
	private static final Logger LOGGER =
			Logger.getLogger(SetupPermanent.class.getName());
	
	// Constantes de commandes de types de Permanent
	public static final String FIXE = "fixe";
	public static final String PROPORTIONNEL = "proportionnel";
	public static final String SOLDER = "solder";
	
	// Constantes d'action
	public static final String VALIDER = "valider";
	public static final String APPLIQUER = "appliquer";
	public static final String SUPPRIMER = "supprimer";
	public static final String QUITTER = "quitter";
	
	/** Un médiateur entre les données de l'interface et les données du modèle.
	 */
	private class DataMediator
	implements ChangeListener, DocumentListener, ItemListener, TableModelListener {
		
		// Le contrôleur de données. Il contrôle l'objet Permanent à éditer.
		PermanentController controller = null;
		
		// Composants graphiques de saisie des données
		private TypeController typeController;			// Contrôleur de type
		private JTextComponent nom, libelle, tiers;		// Composants de texte
		private JCheckBox pointer;						// Pointage
		private JComboBox<Compte> debit, credit;		// Comptes débit/crédit
		private JComboBox<Permanent> dependance;		// Dépendance
		private JComboBox<CompteBancaire> compteASolder;// Compte à solder
		private JSpinner taux;							// Taux
		private PlannerTableModel jours, montants;		// Plannings
		
		public DataMediator(
				TypeController typeController,
				JTextComponent nom,
				JTextComponent libelle,
				JTextComponent tiers,
				JCheckBox pointer,
				JComboBox<Compte> debit,
				JComboBox<Compte> credit,
				PlannerTableModel jours,
				PlannerTableModel montants,
				JComboBox<Permanent> dependance,
				JSpinner taux,
				JComboBox<CompteBancaire> compteASolder) {
			this.typeController = typeController;		// Contrôleur de type
			
			this.nom = nom;								// Zone de nom
			nom.getDocument().addDocumentListener(this);// Écouter
			
			this.libelle = libelle;						// Zone du libellé
			libelle.getDocument().addDocumentListener(	// Écouter
					this);
			
			this.tiers = tiers;							// Zone nom du tiers
			tiers.getDocument().addDocumentListener(	// Écouter
					this);
			
			this.pointer = pointer;						// Case de pointage
			pointer.addChangeListener(this);			// Écouter
			
			this.debit = debit;							// Débit et crédit
			this.credit = credit;
			debit.addItemListener(this);				// Écouter
			credit.addItemListener(this);
			
			this.jours = jours;							// Planning des jours
			jours.addTableModelListener(this);			// Écouter
			
			this.montants = montants;					// Planning des montants
			montants.addTableModelListener(this);		// Écouter
			
			this.dependance = dependance;				// Dépendance
			dependance.addItemListener(this);			// Écouter
			
			this.taux = taux;							// Taux
			taux.addChangeListener(this);
			
			this.compteASolder = compteASolder;			// Compte à solder
			compteASolder.addItemListener(this);		// Écouter
		}// constructeur
		
		/** Renvoie le contrôleur vers lequel pointe actuellement le médiateur.
		 */
		public PermanentController getController() {
			return controller;
		}
		
		/** Remplace le contrôleur de référence. Les données du nouveau
		 * contrôleur sont retranscrites dans l'interface graphique.
		 */
		public void setController(PermanentController controller) {

			// Ignorer la valeur null
			if (controller == null) {
				return;
			}
			
			this.controller = controller;
			
			// Cesser d'écouter pour éviter un callback
			nom.getDocument().removeDocumentListener(this);
			libelle.getDocument().removeDocumentListener(this);
			tiers.getDocument().removeDocumentListener(this);
			pointer.removeChangeListener(this);
			debit.removeItemListener(this);
			credit.removeItemListener(this);
			jours.removeTableModelListener(this);
			montants.removeTableModelListener(this);
			dependance.removeItemListener(this);
			taux.removeChangeListener(this);
			compteASolder.removeItemListener(this);
			
			// Transcrire les données du nouveau contrôleur
			typeController.changeVue(controller.getType()); // Type
			nom.setText(controller.getNom());				// Nom
			libelle.setText(controller.getLibelle());		// Libellé
			tiers.setText(controller.getTiers());			// Tiers
			pointer.setSelected(controller.getPointer());	// Pointage
			credit.setSelectedItem(controller.getCredit());	// Crédit
			jours.setMap(controller.getJours());			// Planning jours
			montants.setMap(controller.getMontants());		// Planning montants
			
			// Le compte à solder modifie aussi le compte débité
			compteASolder.setSelectedItem(					// Compte à solder
					controller.getCompteASolder());
			// On modifie le compté débité seulement après
			debit.setSelectedItem(controller.getDebit());	// Débit
			
			BigDecimal decTaux = controller.getTaux();
			if (decTaux != null) {
				taux.setValue(controller.getTaux());		// Taux non null
			}
			
			/* Mettre à jour la combo box des opérations de dépendance. On
			 * rajoute tous les Permanents, sauf celui qui est sélectionné. */
			dependance.removeAllItems();				// Vider tout
			for (PermanentController pc : controllers) {// Chaque contrôleur
				if (pc != controller) {					// Sauf celui-ci
					dependance.addItem(
							pc.getPermanent());			// Ajouter le Permanent
				}
			}
			dependance.setSelectedItem(					// Sélectionner le bon
					controller.getDependance());
			
			// Réécouter les changements
			nom.getDocument().addDocumentListener(this);
			libelle.getDocument().addDocumentListener(this);
			tiers.getDocument().addDocumentListener(this);
			pointer.addChangeListener(this);
			debit.addItemListener(this);
			credit.addItemListener(this);
			jours.addTableModelListener(this);
			montants.addTableModelListener(this);
			dependance.addItemListener(this);
			taux.addChangeListener(this);
			compteASolder.addItemListener(this);		
		}// setController

		/** Reçoit les modifications de type initiées par l'utilisateur, et les
		 * renvoie au contrôleur de Permanent. */
		public void setType(String type) {
			controller.setType(type);
		}
		
		/** Reçoit les modifications des zones de texte dans l'interface et
		 * les envoie vers le modèle.
		 * @param e	Le DocumentEvent généré par le JTextComponent contenant le
		 * 			texte. */
		private void textChanged(DocumentEvent e) {
			if (e.getDocument() == nom.getDocument()) {
				controller.setNom(nom.getText());			// Nom
			} else if (e.getDocument() == libelle.getDocument()) {
				controller.setLibelle(libelle.getText());	// Libellé
			} else if (e.getDocument() == tiers.getDocument()) {
				controller.setTiers(tiers.getText());		// Tiers
			}
		}// textChanged
		
		/** Interface DocumentListener. Reçoit les notifications de changement
		 * sur le nom.
		 */
		@Override
		public void insertUpdate(DocumentEvent e) {
			textChanged(e);
		}

		/** Interface DocumentListener. Reçoit les notifications de changement
		 * sur le nom.
		 */
		@Override
		public void removeUpdate(DocumentEvent e) {
			textChanged(e);
		}

		@Override
		/** Interface DocumentListener. Aucune implémentation ici.
		 */
		public void changedUpdate(DocumentEvent e) {}

		/** Interface ItemListener. Reçoit les notifications de changements sur
		 * les comptes débité/crédité, sur la dépendance à une autre opération
		 * permanente et sur le compte à solder.
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource() == debit) {
				controller.setDebit((Compte) e.getItem());
			} else if (e.getSource() == credit) {
				controller.setCredit((Compte) e.getItem());
			} else if (e.getSource() == dependance) {
				controller.setDependance((Permanent) e.getItem());
			} else if (e.getSource() == compteASolder) {
				controller.setCompteASolder((CompteBancaire) e.getItem());
			}
		}// itemStateChanged

		/** Interface TableModelListener. Reçoit les notifications de
		 * changements sur le planning des jours, ou des montants fixes.
		 */
		@Override
		public void tableChanged(TableModelEvent e) {
			
			/* Pour les tables de jours et de montants, la classe
			 * PlannerTableModel utilise une Map<Month,Object> pour permettre
			 * l'héritage entre les deux tables.
			 * Il faut transférer les entrées de cette Map vers une
			 * Map<Month,Integer> ou Map<Month,BigDecimal> avant de l'envoyer au
			 * contrôleur de données.
			 */
			if (e.getSource() == jours) {			// Table des jours
				
				// Définir une nouvelle Map pour recevoir les données
				HashMap<Month,Integer> mapJours =
						new HashMap<Month,Integer>();
				
				// Pour chaque entrée de la Map de l'IHM
				for (Entry<Month,Object> entry : jours.getMap().entrySet()) {
					
					// Vérifier la classe de la valeur
					if (entry.getValue() instanceof Integer) {
						
						// Insérer dans la nouvelle Map
						mapJours.put(
								entry.getKey(), (Integer) entry.getValue());
					}// if value Integer
				}// for Entry
				
				// Envoyer la nouvelle Map au contrôleur
				controller.setJours(mapJours);
				
			} else if (e.getSource() == montants) {	// Table des montants
				
				// Définir une nouvelle Map pour recevoir les données
				HashMap<Month,BigDecimal> mapMontants =
						new HashMap<Month,BigDecimal>();
				
				// Pour chaque entrée de la Map de l'IHM
				for (Entry<Month,Object> entry : montants.getMap().entrySet()) {
					
					// Vérifier la classe de la valeur
					if (entry.getValue() instanceof BigDecimal) {
						
						// Insérer dans la nouvelle Map
						mapMontants.put(
								entry.getKey(), (BigDecimal) entry.getValue());
					}// if value BigDecimal
				}// for Entry
				
				// Envoyer la nouvelle Map au contrôleur
				controller.setMontants(mapMontants);
			}// if jours ou montants
		}// tableChanged

		/** Interface ChangeListener. Reçoit les notifications de la case à
		 * cocher pour le pointage automatique ou du spinner taux, et les
		 * renvoie au contrôleur. */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == pointer) {					// Pointage
				controller.setPointer(pointer.isSelected());
			} else if (e.getSource() == taux) {
				controller.setTaux(							// Taux parsé
						new BigDecimal(taux.getValue().toString()));
			}
		}// stateChanged
	}// inner class DataMediator

	/** Un ActionListener qui gère les changements de vues et de boutons en
	 * fonction du type de Permanent. Il peut être appelé par les objets Swing
	 * générant des ActionEvent, ou directement par le programme.
	 * L'objet crée son propre JPanel avec un CardLayout standard.
	 * @author Olivier HAAS */
	private class TypeController implements ActionListener {
		
		/** Map associant les commandes aux boutons, pour permettre la mise à
		 * jour du statut du bouton à l'invocation de la commande. */
		public HashMap<String,AbstractButton> buttonMap =
				new HashMap<String,AbstractButton>();
		
		private CardLayout layout = new CardLayout();	// Le layout modifiable
		public final JPanel panel = new JPanel(layout);	// Le panel à vues
		
		// Composants "débit" à désactiver pour le type "compte à solder"
		private JComponent[] debitComponents;
		
		/** Crée un contrôleur de type qui active ou désactive les composants
		 * spécifiés selon le type. 
		 * @param debit	Les JComponents à désactiver pour le type "compte à
		 * 				solder". Il s'agit en principe des composants du choix
		 * 				du compte débité.
		 */
		public TypeController(JComponent... debitComponents) {
			this.debitComponents = debitComponents;
		}
		
		@Override
		/** Action envoyée par un objet Swing sur action de l'utilisateur. */
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			changeVue(command);						// Extraire la commande
			dataMediator.setType(command);			// Faire suivre au modèle
		}

		/** Action demandée programmatiquement. */
		public void changeVue(String command) {
			AbstractButton bouton;					// Bouton à mettre à jour
			if (buttonMap.containsKey(command)) {	// Commande connue ?
				bouton = buttonMap.get(command);	// Trouve le bon bouton
			} else {
				return;								// Inconnue: ne rien faire
			}
			if (!bouton.isSelected()) {
				bouton.setSelected(true);			// Selectionne le bouton
			}
			layout.show(panel, command);			// Changer de vue
			
			// Activer ou désactiver les JComponents "débit" suivant le type
			boolean actif = !SOLDER.equals(command);	// Inactif si SOLDER
			for (JComponent comp : debitComponents) {	// Appliquer aux JComp
				comp.setEnabled(actif);
			}
		}// changeVue
	}// inner class TypeListener

	/** Le GUI associé. */
	private SimpleGUI gui;
	
	/** Boîte de dialogue principale. */
	private JDialog dialog;
	
	/** Médiateur entre les données de l'IHM et du modèle. */
	private DataMediator dataMediator;
	
	/** La collection ordonnée de contrôleurs de Permanents. */
	private ArrayList<PermanentController> controllers;
	
	/** Liste graphique des Permanents. Elle contient en fait des instances de
	 * PermanentController, transparents pour l'utilisateur. */
	private JList<PermanentController> listPermanents;
	
	/** Construit une boîte de dialogue de gestion des Permanents.
	 * @param owner	La fenêtre parent
	 * @param gui	Le GUI associé (pour mise à jour des boutons)
	 */
	public SetupPermanent(JFrame owner, SimpleGUI gui) {
		this.gui = gui;
		
		// Créer une liste avec les contrôleurs de Permanents
		listPermanents = new JList<>(
				new DefaultListModel<PermanentController>());
		listPermanents.setSelectionMode(	// Mode de sélection
				ListSelectionModel.SINGLE_SELECTION);
		listPermanents
		.addListSelectionListener(this);	// Écouter les changements
		
		// Sélection du type de Permanent (fixe, proportionnel ou dépendant)
		JPanel hautDroite = new JPanel();			// Panel
		ButtonGroup groupeType = new ButtonGroup();	// Groupe de boutons radio
		JRadioButton radioFixe		= new JRadioButton("Fixe");	// Boutons radio
		JRadioButton radioProport	= new JRadioButton("Proportionnel");
		JRadioButton radioSolder	= new JRadioButton("Solde d'un compte");
		radioFixe	.setActionCommand(FIXE);		// Commandes
		radioProport.setActionCommand(PROPORTIONNEL);
		radioSolder	.setActionCommand(SOLDER);
		TypeController typeController =				// Contrôleur de type
				new TypeController();
		JRadioButton[] radios =						// Tous les boutons radio
			{radioFixe, radioProport, radioSolder};
		for (JRadioButton radio : radios) {			// Pour chaque bouton
			radio.addActionListener(typeController);// Ajouter le Listener
			typeController.buttonMap.put(			// Associer commande/bouton
					radio.getActionCommand(), radio);
			groupeType.add(radio);					// Ajouter au groupe
			hautDroite.add(radio);					// Ajouter au panel
		}
		
		// Nom du Permanent
		JLabel labelNom = new JLabel("Nom");		// Étiquette
		JTextField nom = new JTextField();			// Champ de saisie texte
		
		// Libellé de l'opération
		JLabel labelLibelle = new JLabel("Libellé");// Étiquette
		JTextField libelle = new JTextField();		// Champ de saisie texte
		
		// Nom du tiers
		JLabel labelTiers = new JLabel("Tiers");	// Étiquette
		JTextField tiers = new JTextField();		// Champs de saisie texte
		
		// Récupérer tous les comptes dans un tableau ordonné
		ArrayList<Compte> comptes = new ArrayList<Compte>();
		try {
			comptes.addAll(DAOFactory.getFactory().getCompteDAO().getAll());
		} catch (IOException e) {
		}
		Collections.sort(comptes);							// Trier
		Compte[] arrayComptes = new Compte[comptes.size()];	// Mettre en tableau
		comptes.toArray(arrayComptes);
		
		// Box de choix des comptes
		JLabel labelDebit = new JLabel("Débit");				// Etiquettes
		JLabel labelCredit = new JLabel("Crédit");
		final JComboBox<Compte> boxDebit =						// ComboBoxes
				new JComboBox<>(arrayComptes);
		JComboBox<Compte> boxCredit = new JComboBox<>(arrayComptes);
		boxDebit.setRenderer(									// Renderers
				new ComptesComboBoxRenderer());
		boxCredit.setRenderer(new ComptesComboBoxRenderer());
		
		// Case à cocher pour le pointage
		JLabel labelPointage = new JLabel("Pointage");		// Étiquette
		JCheckBox pointer = new JCheckBox();				// Case à cocher
		
		// La table des jours
		PlannerTableModel plannerJours =
				new PlannerTableModel(Integer.class, "Jours");
		JTable tableJours = createPlannerTable(plannerJours);
		
		// Paramètres des opérations à montant fixe
		PlannerTableModel plannerMontants =					// Modèle
				new PlannerTableModel(BigDecimal.class, "Montants");
		JTable tableMontants = createPlannerTable(plannerMontants);
		tableMontants.setDefaultRenderer(				// Renderer BigDecimals
				BigDecimal.class,new FinancialTable.MontantTableCellRenderer());
		
		// Paramètres des opérations à montant proportionnel à une autre
		JLabel labelTaux = new JLabel("Taux");				// Etiquette taux				
		JSpinner taux = new JSpinner(						// Saisie du taux
				new SpinnerNumberModel(0.0, 0.0, null, 0.1));
		taux.setEditor(										// Affichage du taux
				new JSpinner.NumberEditor(taux, "0.00 '%'"));
		JLabel labelPermanent =								// Label opération
				new JLabel("Opération référente");
		JComboBox<Permanent> dependance =					// Box dépendance
				new JComboBox<>();
		
		// Paramètres des opérations soldant un compte
		JLabel labelSolder = new JLabel("Compte à solder");
		JComboBox<CompteBancaire> compteASolder =		// Box comptes à solder
				new JComboBox<>();
		for (Compte c : arrayComptes) {					// Remplir la liste
			if (c instanceof CompteBancaire) {// Seulement des comptes bancaires
				compteASolder.addItem((CompteBancaire) c);
			}
		}
		compteASolder.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boxDebit.setSelectedItem(e.getItem());	// Ajuster compte débité
			}
		});// classe anonyme ItemListener
		
		// Créer le médiateur qui traduira les données
		dataMediator = new DataMediator(
				typeController,
				nom,
				libelle,
				tiers,
				pointer,
				boxDebit,
				boxCredit,
				plannerJours,
				plannerMontants,
				dependance,
				taux,
				compteASolder);
		
		// Boutons de validation
		JButton valider		= new JButton("Valider");	// Bouton Valider
		JButton appliquer	= new JButton("Appliquer");	// Bouton Appliquer
		JButton supprimer	= new JButton("Supprimer");	// Bouton Supprimer
		JButton quitter		= new JButton("Quitter");	// Bouton Quitter
		valider		.setActionCommand(VALIDER);			// Commandes
		appliquer	.setActionCommand(APPLIQUER);
		supprimer	.setActionCommand(SUPPRIMER);
		quitter		.setActionCommand(QUITTER);
		valider		.addActionListener(this);			// Écouter
		appliquer	.addActionListener(this);
		supprimer	.addActionListener(this);
		quitter		.addActionListener(this);
		
		// Charger la liste principale
		fillPermanentList(null);
		
		// Disposer tout ensemble
		
		// Liste des permanents dans un ScrollPane
		JScrollPane scrollList = new JScrollPane(listPermanents);
		scrollList.setPreferredSize(
				new Dimension(150, scrollList.getPreferredSize().height));
		
		// Panneau de choix du type
		hautDroite.setLayout(						// Agencement
				new BoxLayout(hautDroite, BoxLayout.PAGE_AXIS));
		TitledBorder titre =						// Bordure avec titre
				BorderFactory.createTitledBorder("Type d'opération");
		Font font = titre.getTitleFont();
        if (font == null) {
        	font = UIManager.getFont("TitledBorder.font");
        }
        if (font == null) {
        	font = hautDroite.getFont();
        }
		font = font.deriveFont(Font.ITALIC);		// Police italique(pas gras)
		titre.setTitleFont(font);
		
		hautDroite.setBorder(titre);				// Appliquer la bordure

		// Panneau du nom, libellé, tiers, pointage, choix des comptes
		JPanel panelComptes = new JPanel();			// Panneau
		GroupLayout layoutComptes =					// Agencement
				new GroupLayout(panelComptes);
		panelComptes.setLayout(layoutComptes);
		layoutComptes.setAutoCreateGaps(true);
		layoutComptes.setAutoCreateContainerGaps(true);
		layoutComptes.setHorizontalGroup(layoutComptes.createSequentialGroup()
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.TRAILING, false)
						.addComponent(labelNom)
						.addComponent(labelLibelle)
						.addComponent(labelTiers)
						.addComponent(labelDebit)
						.addComponent(labelCredit))
						.addComponent(labelPointage)
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.LEADING, false)
						.addComponent(nom)
						.addComponent(libelle)
						.addComponent(tiers)
						.addComponent(boxDebit)
						.addComponent(boxCredit)
						.addComponent(pointer)));
		layoutComptes.setVerticalGroup(layoutComptes.createSequentialGroup()
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelNom)
						.addComponent(nom))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelLibelle)
						.addComponent(libelle))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelTiers)
						.addComponent(tiers))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelDebit)
						.addComponent(boxDebit))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelCredit)
						.addComponent(boxCredit))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelPointage)
						.addComponent(pointer)));

		// Panneau des opérations proportionnelles
		JPanel propPanel = new JPanel();
		GroupLayout layoutProp = new GroupLayout(propPanel);// Agencement
		propPanel.setLayout(layoutProp);
		layoutProp.setAutoCreateContainerGaps(true);
		layoutProp.setAutoCreateGaps(true);
		layoutProp.setHorizontalGroup(layoutProp.createSequentialGroup()
				.addGroup(layoutProp.createParallelGroup(
						GroupLayout.Alignment.TRAILING, false)
						.addComponent(labelTaux)
						.addComponent(labelPermanent))
				.addGroup(layoutProp.createParallelGroup(
						GroupLayout.Alignment.LEADING, false)
						.addComponent(taux)
						.addComponent(dependance)));
		layoutProp.setVerticalGroup(layoutProp.createSequentialGroup()
				.addGroup(layoutProp.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelTaux)
						.addComponent(taux))
				.addGroup(layoutProp.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelPermanent)
						.addComponent(dependance)));
		
		// Panneau des opérations soldant un compte
		JPanel solderPanel = new JPanel();
		solderPanel.add(labelSolder);
		solderPanel.add(compteASolder);

		// Les paramètres variables suivant le type d'instance
		JPanel cardPane = typeController.panel;				// Panel à vues
		cardPane.add(new JScrollPane(tableMontants), FIXE);	// Fixes
		cardPane.add(propPanel, PROPORTIONNEL);				// Proportionnels
		cardPane.add(solderPanel, SOLDER);					// A solder
		
		// Un panneau transversal en deux cases pour les détails
		JPanel transversalPanel = new JPanel(new GridLayout(1,2));
		transversalPanel.add(								// Table des jours
				new JScrollPane(tableJours), BorderLayout.WEST);
		transversalPanel.add(cardPane);						//Panneau "carte"

		// Panneau des boutons de validation
		JPanel validationPanel = new JPanel();				// Panneau
		validationPanel.setLayout(							// Agencement
				new BoxLayout(validationPanel, BoxLayout.LINE_AXIS));
		validationPanel.add(supprimer);						// Bouton Supprimer
		validationPanel.add(Box.createHorizontalGlue());	// Pousser à droite
		validationPanel.add(appliquer);						// Bouton Appliquer
		validationPanel.add(valider);						// Bouton Valider
		validationPanel.add(quitter);						// Bouton Quitter
		
		// Panneau d'édition
		JPanel editionPane = new JPanel();
		editionPane.setLayout(						// Agencement vertical
				new BoxLayout(editionPane, BoxLayout.PAGE_AXIS));
		editionPane.add(hautDroite);				// Panel choix du type
		editionPane.add(panelComptes);				// Panel choix du compte
		editionPane.add(transversalPanel);
		editionPane.add(validationPanel);
		
		// Panneau général
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(								// Créer une marge
				BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(scrollList, BorderLayout.WEST);	// Liste des Permanent
		main.add(editionPane);						// Panneau d'édition
		
		// Lier la touche ESC à l'action de quitter
		UniversalAction actionQuitter = new UniversalAction(this, QUITTER);
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
		
//		boxDebit.setSelectedItem(null);
	}// constructeur
	
	/** Renvoie une table de planning par mois.
	 * La table gère l'affichage et l'édition des mois.
	 * @param model	Le PlannerTableModel à utiliser
	 * @return		Une table appuyée sur le PlannerTableModel.
	 * @see	PlannerTableModel
	 */
	private JTable createPlannerTable(PlannerTableModel model) {
		JTable table = new JTable(model);
//		table.setDefaultRenderer(						// Renderer des mois
//				Month.class, new FinancialTable.MonthTableCellRenderer());
		table.setDefaultEditor(							// Editor des mois
				Month.class, new MonthCellEditor());
		return table;
	}// createPlannerTable
	
	/** Remplit la liste graphique avec des contrôleurs de tous les Permanents.
	 * 
	 * @param selection	Le Permanent à sélectionner après la mise à jour. Si
	 * 					<code>null</code>, sélectionne l'item "Nouveau..."
	 */
	private void fillPermanentList(Permanent selection) {
		
		// La liste des permanents
		Collection<Permanent> permanents = new ArrayList<Permanent>();
		
		// Ajouter un null au début pour autoriser une nouvelle saisie.
		permanents.add(null);
		
		// Obtenir tous les Permanents
		try {
			for (Permanent p :
				DAOFactory.getFactory().getPermanentDAO().getAll()) {
				
				permanents.add(p);
			}// for permanents
			
		} catch (IOException e) {
		}// try
		
		// Liste des contrôleurs de Permanents
		controllers = new ArrayList<PermanentController>();
		
		// Remplir la liste de contrôleurs
		PermanentController selected = null;
		for (Permanent p : permanents) {
			PermanentController controller =			// Nouveau contrôleur
					new PermanentController(p);
			controllers.add(controller);				// Ajouter
			if (p == selection) {
				selected = controller;					// Repérer la sélection
			}
		}// for Permanent
		
		// Trier
		Collections.sort(controllers);
		
		// Le modèle
		DefaultListModel<PermanentController> listModel =
				new DefaultListModel<>();
		for (PermanentController pc : controllers) {	// Remplir le modèle
			listModel.addElement(pc);
		}
		listPermanents.setModel(listModel);				// Affecter le modèle
		
		// Sélectionner le bon item
		if (selection == null) {						// Pas de sélection
			listPermanents.setSelectedIndex(
					0);									//Sélectionne le premier
		} else {
			listPermanents.setSelectedValue(			// Ou l'item spécifié
					selected, true);
		}
	}// fillPermanentList
	
	/** Actualise l'interface graphique avec les paramètres du Permanent
	 * nouvellement sélectionné.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		// Faire afficher les données de la nouvelle sélection
		dataMediator.setController(
				(PermanentController) listPermanents.getSelectedValue());
	}// valueChanged

	/** Reçoit les actions des boutons de validation. */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		// Mémoriser la sélection actuelle
		PermanentController selected =
				dataMediator.getController();			//Contrôleur sélectionné
		Permanent selection = selected.getPermanent();	// Permanent lié
		
		// Appliquer les changements si nécessaire
		if (VALIDER.equals(command) || APPLIQUER.equals(command)) {
			Permanent p;								// Curseur de Permanent
			
			// Pour chaque contrôleur
			for (PermanentController pc : controllers) {
				try {
					// Appliquer les modifs
					p = pc.applyChanges();
					
				} catch (IOException e1) {
					LOGGER.log(
							Level.SEVERE,
							"Impossible de sauvegarder l'opération\n" + pc, e1);
					
					// Arrêter ici sans recharger les données
					return;
				}// try
				
				// Actualiser la sélection si besoin
				if (pc == selected) {
					selection = p;
				}
			}// for controllers
		}// if Valider ou Appliquer
		
		// Supprimer si nécessaire
		if (SUPPRIMER.equals(command)
				&& JOptionPane.showConfirmDialog(		// Demander confirmation
						dialog,
						"Voulez-vous vraiment supprimer l'opération permanente "
						+ selected + " ?",
						"Supprimer une opération permanente",
						JOptionPane.YES_NO_OPTION)
					== JOptionPane.YES_NO_OPTION) {		// Réponse OUI
			try {
				selected.deletePermanent();				// Effacer
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE,
						"Impossible de supprimer " + selected, e1);
			}// try
		}// if Supprimer et confirmation
		
		// Quitter ou recharger les données
		if (QUITTER.equals(command) || VALIDER.equals(command)) {
			
			// Vérifier s'il y a des modifications non enregistrées
			boolean modified = false;					// Marqueur de modifs
			for (PermanentController pc : controllers) {
				modified = modified || pc.isModified();	// Contrôleur modifié ?
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
				gui.dataModified();					// Prévenir du changement
			}
		} else {
			fillPermanentList(selection);			// Recharger les données
		}// if Quitter ou Valider
	}// actionPerformed
}// class SetupPermanent

/** Un contrôleur de Permanent. Permet de pré-visualiser les modifications à
 * apporter à un Permanent.
 * Cette classe est externe à SetupPermanent pour éviter que celle-ci n'accède
 * aux propriétés privés du PermanentController, au lieu de passer par les
 * setters.
 */
class PermanentController implements Comparable<PermanentController> {
	private Permanent permanent = null;			// Permanent à modifier
	private boolean modified = false;			// Marqueur de modifications
	
	// Propriétés à éditer
	private String type = null;
	private String nom = null;
	private Compte debit = null, credit = null;
	private String libelle = null, tiers = null;
	private boolean pointer = false;
	private Map<Month,Integer> jours = null;
	private Map<Month,BigDecimal> montants = null;
	private Permanent dependance = null;
	private BigDecimal taux;
	private CompteBancaire compteASolder = null;
	
	/** Construit un contrôleur contenant les données actuelles du Permanent
	 * spécifié.
	 * @param p	Le Permanent à utiliser. Utiliser null pour la définition
	 * 			d'un nouveau Permanent.
	 */
	public PermanentController(Permanent p) {
		permanent = p;								// Mémoriser le Permanent
		refresh();									// Récupérer ses propriétés
	}
	
	/** Rafraîchit les données à partir des propriétés du Permanent actuel.
	 */
	private void refresh() {
		if (permanent != null) {			// Permanent pré-existant (non null)
			nom				= permanent.nom;		// Mémoriser ses propriétés
			debit			= permanent.debit;
			credit			= permanent.credit;
			libelle			= permanent.libelle;
			tiers			= permanent.tiers;
			pointer			= permanent.pointer;
			jours			= permanent.jours;

			// Déterminer le type et appliquer les propriétés particulières
			if (permanent instanceof PermanentFixe) {
				type = SetupPermanent.FIXE;
				montants = ((PermanentFixe) permanent).montants;

			} else if (permanent instanceof PermanentProport) {
				type = SetupPermanent.PROPORTIONNEL;
				dependance = ((PermanentProport) permanent).dependance;
				taux = ((PermanentProport) permanent).taux;
				
			} else if (permanent instanceof PermanentSoldeur) {				
				type = SetupPermanent.SOLDER;
			}// if type
			
		} else {									// Pas de Permanent
			type = SetupPermanent.FIXE;				// Type FIXE par défaut
			taux = new BigDecimal("2");				// Taux par défaut
		}// if permanent not null
	}// constructeur
	
	public Permanent getPermanent() {
		return permanent;
	}// getPermanent
	
	// Getters
	public String getType()						{return type;}
	public String getNom()						{return nom;}
	public Compte getDebit()					{return debit;}
	public Compte getCredit()					{return credit;}
	public String getLibelle()					{return libelle;}
	public String getTiers()					{return tiers;}
	public boolean getPointer()					{return pointer;}
	public Map<Month,Integer> getJours()		{return jours;}
	public Map<Month,BigDecimal> getMontants()	{return montants;}
	public Permanent getDependance()			{return dependance;}
	public BigDecimal getTaux()					{return taux;}
	public CompteBancaire getCompteASolder()	{return compteASolder;}
	
	// Setters. Mémoriser l'existence d'une modif.
	public void setType(String type) {
		this.type = type;
		modified = true;
	}// setType
	
	public void setNom(String nom) {
		this.nom = nom;
		modified = true;
	}// setNom
	
	public void setDebit(Compte debit) {
		this.debit = debit;
		modified = true;
	}// setDebit
	
	public void setCredit(Compte credit) {
		this.credit = credit;
		modified = true;
	}// setCredit
	
	public void setLibelle(String libelle) {
		this.libelle = libelle;
		modified = true;
	}// setLibelle
	
	public void setTiers(String tiers) {
		this.tiers = tiers;
		modified = true;
	}// setTiers
	
	public void setPointer(boolean pointer) {
		this.pointer = pointer;
		modified = true;
	}// setPointer
	
	public void setJours(Map<Month,Integer> jours) {
		this.jours = jours;
		modified = true;
	}// setJours
	
	public void setMontants(Map<Month,BigDecimal> montants) {
		this.montants = montants;
		modified = true;
	}// setMontants
	
	public void setDependance(Permanent dependance) {
		this.dependance = dependance;
		modified = true;
	}// setDependance
	
	public void setTaux(BigDecimal taux) {
		this.taux = taux;
		modified = true;
	}// setTaux
	
	public void setCompteASolder(CompteBancaire compteASolder) {
		this.compteASolder = compteASolder;
		modified = true;
	}// setCompteASolder
	
	public boolean isModified() {
		return modified;
	}// isModified
	
	/** Applique les modifications au Permanent et envoie la nouvelle
	 * version au DAO.
	 * 
	 * @return	Le nouveau Permanent enregistré dans le DAO. S'il n'y a pas de
	 * 			modifications, c'est la même instance qu'auparavant.
	 * 
	 * @throws IOException
	 */
	public Permanent applyChanges() throws IOException {
		if (!modified)									// Si rien n'a changé
			return permanent;							// Ne rien faire

		// Instancier un nouveau Permanent en remplacement de l'actuel
		Permanent newPermanent = null;					// Déclarer le nouveau
		Integer id = (permanent == null)				// Id à utiliser
				? null : permanent.id;
		if (SetupPermanent.FIXE.equals(type)) {
			newPermanent = new PermanentFixe(			// Montants fixes
					id, nom, debit, credit, libelle, tiers, pointer, jours,
					montants);
		} else if (SetupPermanent.PROPORTIONNEL.equals(type)) {
			newPermanent = new PermanentProport(		// Proportionnel
					id, nom, debit, credit, libelle, tiers, pointer, jours,
					dependance, taux);
		} else if (SetupPermanent.SOLDER.equals(type)) {
			newPermanent = new PermanentSoldeur(		// Compte à solder
					id, nom, (CompteBancaire) debit, credit, libelle, tiers,
					pointer, jours);
		}

		// Enregistrer dans le DAO
		PermanentDAO dao = DAOFactory.getFactory().getPermanentDAO();
		if (permanent == null) {						// Pas d'ancien objet
			permanent = dao.add(newPermanent);			// Insérer et mémoriser
		} else {										// Objet existant
			dao.update(newPermanent);					// Remplacer
			permanent = dao.get(id);					// Mémoriser le nouveau
		}

		// Rafraîchir les données
		refresh();
		
		// Réinitialiser le marqueur ("non modifié")
		modified = false;
		
		return permanent;
	}// applyChanges
	
	/** Supprime le Permanent. 
	 * @throws IOException */
	public void deletePermanent() throws IOException {
		DAOFactory.getFactory().getPermanentDAO().remove(permanent.id);
	}
	
	/** Applique la relation d'ordre des Permanents au contrôleurs de
	 * Permanents. Si on contrôleur est vide ("Nouveau..."), il passe avant. */
	@Override
	public int compareTo(PermanentController controller) {
		Permanent permanent2 = controller.getPermanent();
		if (permanent != null && permanent2 != null) {
			return permanent.compareTo(permanent2);	// Cas général
		} else if (permanent == null && permanent2 != null) {
			return -1;								// Le null en premier
		} else if (permanent != null && permanent2 == null) {
			return 1;								// Le non-null en deuxième
		} else {
			return 0;								// Deux null: égalité !
		}
	}// compareTo
	
	/** Affiche le nom du Permanent vers lequel pointe l'objet. */
	@Override
	public String toString() {
		if (permanent == null) {					// Pas de Permanent
			return "Nouveau...";					// Texte pour un nouveau
		} else {
			return permanent.toString();			// Sinon, nom du Permanent
		}
	}// toString
}// package-private class PermanentController

@SuppressWarnings("serial")
/** Un TableModel pour la table des jours. */
class PlannerTableModel extends AbstractTableModel {

	private final Class<?> classe;
	private String titre;
	private TreeMap<Month,Object> map = new TreeMap<Month,Object>();// La map
	private Month moisSaisi = null;		// Mois dans la ligne de saisie
	private Object valeurSaisie = null;	//Valeur du mois dans la ligne de saisie
	
	public PlannerTableModel(Class<?> classe, String titre) {
		this.classe = classe;
		this.titre = titre;
	}
	
	/** Retourne les données de la ligne spécifiée de la map.
	 * @param index	Index de la map interne (et non du modèle). */
	private Entry<Month,Object> getEntryAt(int index) {
		Entry<Month,Object> entry = null;					// La variable
		Iterator<Entry<Month,Object>> iterator =
				map.entrySet().iterator();					// Itérateur
		int i=0;											// Compteur
		while (iterator.hasNext() && i++ <= index) {
			entry = iterator.next();						// Suivant
		}
		return entry;
	}// getEntryAt
	
	@Override
	public String getColumnName(int columnIndex) {
		return (columnIndex == 0) ? "Mois" : titre;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return (columnIndex == 0) ? Month.class : classe;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		
		// Récupérer la saisie dans le type adéquat (ou null)
		Month month = null;							// Mois null par défaut
		Object valeur = null;						// Valeur null par défaut
		if (columnIndex == 0 && aValue instanceof Month) {
			month = (Month) aValue;					// Le mois
		} else if (columnIndex == 1 && classe.isInstance(aValue)) {
			valeur = aValue;						// La valeur du mois
		}
		
		// Modifier les données
		if (rowIndex == 0) {						// Ligne de saisie ?
			if (columnIndex == 0) {
				moisSaisi = month;					// Saisie du mois
			} else if (columnIndex == 1) {
				valeurSaisie = valeur;				// Saisie valeur du mois
			}
			
			// Si toutes les données sont saisies
			if (moisSaisi != null && valeurSaisie != null) {
				map.put(moisSaisi, valeurSaisie);	// Mettre dans une entrée
				moisSaisi = null;					// Vider la ligne de saisie
				valeurSaisie = null;
			}
			
		} else {									// Ligne pré-remplie
			Entry<Month,Object> entry =
					getEntryAt(rowIndex-1);			// Entrée existante
			if (columnIndex == 0) {					// Changement de mois ?
				map.remove(entry.getKey());			// Supprimer l'entrée
				if (month != null) {				// Si nouveau mois
					map.put(month, entry.getValue());// Nouvelle entrée 
				}// if month null
			} else if (columnIndex == 1) {			// Changement de jour ?
				if (valeur == null) {				// Saisie de valeur effacée?
					map.remove(entry.getKey());		// Supprimer l'entrée
				} else {
					map.put(entry.getKey(), valeur);// Changer la valeur du mois
				}// if valeur null
			}// if colonne
		}// if ligne de saisie
		fireTableDataChanged();						// Avertir des changements
	}// setValueAt

	@Override
	public int getRowCount() {
		return map.size()+1;		// La taille de la map + une ligne de saisie
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		// Ligne de saisie
		if (rowIndex == 0) {
			// Le mois ou le jour saisi
			return (columnIndex == 0) ? moisSaisi : valeurSaisie;
		}
		
		// L'entrée de la ligne (n-1 à cause de la ligne de saisie)
		Entry<Month,Object> entry = getEntryAt(rowIndex-1);
		
		// Retourner le mois ou le jour du mois, suivant la colonne
		return (columnIndex == 0) ? entry.getKey() : entry.getValue();
	}// getValueAt
	
	/** Renvoie la Map actuelle. */
	public Map<Month,Object> getMap() {
		return map;
	}
	
	/** Remplace les données actuelles par celles qui sont contenues dans la Map
	 * spécifiée.
	 * @param jours	La Map contenant les valeurs à utiliser. L'objet fourni en
	 * 				paramètre n'est pas modifié. Par contrat, cette	Map ne doit
	 * 				contenir comme valeurs que des objets de la	classe fournie
	 * 				au constructeur. Sinon, le comportement est indéfini.
	 */
	public void setMap(Map<Month,? extends Object> jours) {
		map = (jours == null)
				? new TreeMap<Month,Object>()
				: new TreeMap<Month,Object>(jours);		// Nouvelles données
		fireTableDataChanged();							// Avertir du changement
	}
}// class PlannerTableModel

/** Un TableCellEditor pour la classe Month. */
@SuppressWarnings("serial")
class MonthCellEditor extends DefaultCellEditor {
	
	/** Construit un éditeur avec JComboBox. */
	public MonthCellEditor() {
		super(new JComboBox<Month>());
	}

	@Override
	/** Renvoie une JComboBox pré-remplie avec les 12 mois avant et après le
	 * mois de référence. Si le mois de référence est null, par rapport à la
	 * date du jour. */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		// ComboBox générée par la classe mère
		@SuppressWarnings("unchecked")
		JComboBox<Month> comboBox =
				(JComboBox<Month>) super.getTableCellEditorComponent(
						table, value, isSelected, row, column);
		
		comboBox.removeAllItems();				// Supprimer tous les items
		Month reference = (value instanceof Month) ?
				(Month) value : new Month();	// Mois de référence (ou actuel)
		
		// Re-remplir, de -12 mois à +12 mois
		for (int i=-12; i<=12; i++) {
			comboBox.addItem(reference.getTranslated(i));
		}
		
		// Sélectionner le mois de référence par défaut
		comboBox.setSelectedItem(reference);
		
		return comboBox;
	}// getTableCellEditorComponent
}// class MonthCellEditor