package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.Compte;
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
import java.beans.EventHandler;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.table.AbstractTableModel;
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
					ItemListener.class, this, "setPointer", "itemChange"));
			jours.addTableModelListener(EventHandler.create(
					TableModelListener.class, this, "joursChanged"));
			montants.addTableModelListener(EventHandler.create(
					TableModelListener.class, this, "montantsChanged"));
			taux.addChangeListener(EventHandler.create(
					ChangeListener.class, this, "tauxChanged"));
			
			/*
			 * Les ItemListener sont notifiés que lorsque la modification vient
			 * de l'utilisateur, pas en cas de changement programmatique. On
			 * peut donc renvoyer vers le contrôleur sans tester le drapeau
			 * updating.
			 */
			debit.addItemListener(EventHandler.create(
					ItemListener.class, controller, "setDebit", "item"));
			credit.addItemListener(EventHandler.create(
					ItemListener.class, controller, "setCredit", "item"));
			dependance.addItemListener(EventHandler.create(
					ItemListener.class, controller, "setDependance", "item"));
			compteASolder.addItemListener(EventHandler.create(
					ItemListener.class, controller, "setCompteASolder","item"));
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
			
			// Transcrire les données du nouveau contrôleur
			updating = true;
			typeController.changeVue(controller.getType());
			nom.setText(controller.getNom());
			libelle.setText(controller.getLibelle());
			tiers.setText(controller.getTiers());
			pointer.setSelected(controller.getPointer());
			credit.setSelectedItem(controller.getCredit());
			jours.setMap(controller.getJours());
			montants.setMap(controller.getMontants());
			
			// Le compte à solder modifie aussi le compte débité
			compteASolder.setSelectedItem(controller.getCompteASolder());
			// On modifie le compté débité seulement après
			debit.setSelectedItem(controller.getDebit());
			
			// Taux
			BigDecimal decTaux = controller.getTaux();
			if (decTaux != null)
				taux.setValue(controller.getTaux());	// Taux non null
			
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
		 * À l'issue de la méthode, lé dépendance actuelle est sélectionnée,
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
			if (updating) {
				return;
			} else if (state == ItemEvent.SELECTED) {
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
		// FIXME L'héritage entre les deux tables de PlannerTableModel est ici un obstacle
		public void joursChanged() {
			HashMap<Month,Integer> mapJours = new HashMap<>();
			for (Entry<Month,Object> entry : jours.getMap().entrySet()) {
				Object value = entry.getValue();
				if (value instanceof Integer) {
					mapJours.put(entry.getKey(), (Integer) value
							);
				}
			}
			controller.setJours(mapJours);
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
			HashMap<Month,BigDecimal> mapMontants = new HashMap<>();
			for (Entry<Month,Object> entry : montants.getMap().entrySet()) {
				Object value = entry.getValue();
				if (value instanceof BigDecimal) {
					mapMontants.put(entry.getKey(), (BigDecimal) value);
				}
			}
			controller.setMontants(mapMontants);
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
	 * Un <code>ActionListener</code> qui gère les changements de vues et de
	 * boutons en fonction du type de <code>Permanent</code>. Il peut être
	 * appelé par les objets Swing générant des <code>ActionEvent</code>, ou
	 * directement par le programme. L'objet crée son propre <code>JPanel</code>
	 * avec un <code>CardLayout</code> standard.
	 * 
	 * @author Olivier HAAS
	 */
	private class TypeController implements ActionListener {
		
		/**
		 * <code>Map</code> associant les commandes aux boutons, pour permettre
		 * la mise à jour du statut du bouton à l'invocation de la commande.
		 */
		private final Map<String, AbstractButton> buttonMap = new HashMap<>();
		
		/**
		 * Le layout modifiable.
		 */
		private final CardLayout layout = new CardLayout();
		
		/**
		 * Le panel à vues.
		 */
		private final JPanel panel = new JPanel(layout);
		
		/**
		 * Composants "débit" à désactiver pour le type "compte à solder".
		 */
		private JComponent[] debitComponents;
		
		/**
		 * Crée un contrôleur de type qui active ou désactive les composants
		 * spécifiés selon le type. 
		 * 
		 * @param debit	Les <code>JComponent</code>s à désactiver pour le type
		 * 				"compte à solder". Il s'agit en principe des composants
		 * 				du choix du compte débité.
		 */
		public TypeController(JComponent... debitComponents) {
			this.debitComponents = debitComponents;
		}
		
		/**
		 * Action envoyée par un objet Swing sur action de l'utilisateur.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			changeVue(command);						// Extraire la commande
			
			// Faire suivre au contrôleur
			dataMediator.getController().setType(command);
		}
	
		/**
		 * Action demandée programmatiquement.
		 */
		public void changeVue(String command) {
			AbstractButton bouton;					// Bouton à mettre à jour
			if (!buttonMap.containsKey(command))
				return;
			bouton = buttonMap.get(command);
			
			if (!bouton.isSelected())
				bouton.setSelected(true);
			
			layout.show(panel, command);			// Changer de vue
			
			// Activer ou désactiver les JComponents "débit" suivant le type
			boolean actif = !SOLDER.equals(command);
			for (JComponent comp : debitComponents)
				comp.setEnabled(actif);
		}
	}// inner class TypeListener

	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SetupPermanent.class.getName());
	
	// Constantes de commandes de types de Permanent
	public static final String FIXE = "fixe";
	public static final String PROPORTIONNEL = "proportionnel";
	public static final String SOLDER = "solder";
	
	// Composants graphiques de saisie des données
	
	/**
	 * Contrôleur de type.
	 */
	private final TypeController typeController = new TypeController();
	
	/**
	 * Le bouton de sélection du type d'opérations permanentes à montants fixes.
	 */
	private final JRadioButton radioFixe = new JRadioButton("Fixe");
	
	/**
	 * Le bouton de sélection du type d'opérations permanentes proportionnelles
	 * à une autre opération.
	 */
	private final JRadioButton radioProport = new JRadioButton("Proportionnel");
	
	/**
	 * Le bouton de sélection du type d'opérations permanentes dont le rôle est
	 * de solder un compte spécifique.
	 */
	private final JRadioButton radioSolder =
			new JRadioButton("Solde d'un compte");
	
	/**
	 * Champ de saisie du nom.
	 */
	private final JTextComponent nom = new JTextField();;
	
	/**
	 * Champ de saisie du libellé.
	 */
	private final JTextComponent libelle = new JTextField();;
	
	/**
	 * Champ de saisie du tiers.
	 */
	private final JTextComponent tiers = new JTextField();;
	
	/**
	 * Case à cocher pour le pointage automatique de l'opération.
	 */
	private final JCheckBox pointer = new JCheckBox();
	
	/**
	 * Liste déroulante pour choisir le compte débité.
	 */
	private final JComboBox<Compte> debit = new JComboBox<>();
	
	/**
	 * Liste déroulante pour choisir le compte crédité.
	 */
	private final JComboBox<Compte> credit = new JComboBox<>();
	
	/**
	 * Liste déroulante pour choisir l'opération dont dépend celle-ci.
	 */
	private final JComboBox<Permanent> dependance = new JComboBox<>();
	
	/**
	 * Liste déroulante pour choisir le compte à solder.
	 */
	private final JComboBox<Compte> compteASolder = new JComboBox<>();
	
	/**
	 * Champ de saisie du taux, pour une opération dépendante d'une autre.
	 * <p>
	 * Ce champ permet une saisie à la souris avec une précision d'une décimale.
	 */
	private final JSpinner taux =
			new JSpinner(new SpinnerNumberModel(0.0, 0.0, null, 0.1));
	
	/**
	 * Planning des jours du mois.
	 */
	private final PlannerTableModel jours =
			new PlannerTableModel(Integer.class, "Jours");
	
	/**
	 * Planning des montants.
	 */
	private final PlannerTableModel montants =
			new PlannerTableModel(BigDecimal.class, "Montants");
	
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
	 * 				comptes.
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
		
		// Configurer les composants de saisie
		Compte[] comptes = getComptes();
		initComptesComboBox(debit, comptes);
		initComptesComboBox(credit, comptes);
		initTypeButtons();
		taux.setEditor(new JSpinner.NumberEditor(taux, "0.00 '%'"));
		initComptesASolder(comptes);
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
	 * Renvoie tous les comptes dans un tableau trié.
	 * 
	 * @return	Un tableau contenant tous les comptes triés par ordre naturel.
	 * 
	 * @throws IOException
	 */
	private static Compte[] getComptes() throws IOException {
		Collection<Compte> comptes =
				DAOFactory.getFactory().getCompteDAO().getAll();
		Compte[] arrayComptes = comptes.toArray(new Compte[comptes.size()]);
		Arrays.sort(arrayComptes);
		return arrayComptes;
	}
	
	/**
	 * Configure une liste déroulante des comptes.
	 * <p>
	 * Les comptes sont insérés dans la liste déroulante, et l'affichage est
	 * paramétré avec un Renderer approprié.<br>
	 * Si la liste déroulante contient déjà quelque chose, son contenu est
	 * supprimé.
	 * 
	 * @param combo		La liste déroulante des comptes.
	 * @param comptes	Les comptes à insérer dans la liste déroulante.
	 */
	private static void initComptesComboBox(JComboBox<Compte> combo,
			Compte[] comptes) {
		combo.setModel(new DefaultComboBoxModel<>(comptes));
		combo.setRenderer(new ComptesComboBoxRenderer());
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
		JList<PermanentController> list =
				new JList<>(new DefaultListModel<PermanentController>());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(EventHandler.create(
				ListSelectionListener.class, dataMediator, "setController",
				"selectedValue"));
		return list;
	}
	
	/**
	 * Renvoie une table de planning par mois.
	 * La table gère l'affichage et l'édition des mois.
	 * 
	 * @param model	Le <code>PlannerTableModel</code> à utiliser
	 * @return		Une table appuyée sur le <code>PlannerTableModel</code>.
	 * 
	 * @see	{@link PlannerTableModel}
	 */
	private static JTable createPlannerTable(PlannerTableModel model) {
		JTable table = new JTable(model);
		
		// Éditeur spécifique pour les mois
		table.setDefaultEditor(Month.class, new MonthCellEditor());
		
		table.setDefaultRenderer(BigDecimal.class,
				new FinancialTable.MontantTableCellRenderer());
		return table;
	}
	
	/**
	 * Crée un panneau contenant les boutons de choix du type d'opération.
	 * 
	 * @return	Un panneau.
	 */
	private static JPanel createTypePanel() {
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.PAGE_AXIS));
		TitledBorder titre =
				BorderFactory.createTitledBorder("Type d'opération");
		
		// Récupérer la police du titre (ou une autre) et la mettre en italique
		Font font = titre.getTitleFont();
		if (font == null) {
			font = UIManager.getFont("TitledBorder.font");
		}
		if (font == null) {
			font = typePanel.getFont();
		}
		font = font.deriveFont(Font.ITALIC);		// Police italique(pas gras)
		titre.setTitleFont(font);
	
		typePanel.setBorder(titre);					// Appliquer la bordure
		return typePanel;
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
		
		// Sélection du type de Permanent (fixe, dépendant ou soldeur)
		JPanel typePanel = createTypePanel();
		typePanel.add(radioFixe);
		typePanel.add(radioProport);
		typePanel.add(radioSolder);
		
		// Les tables de jours et de montants
		JTable tableJours = createPlannerTable(jours);
		JTable tableMontants = createPlannerTable(montants);
		
		// Étiquettes
		JLabel labelNom = new JLabel("Nom");
		JLabel labelLibelle = new JLabel("Libellé");
		JLabel labelTiers = new JLabel("Tiers");
		JLabel labelDebit = new JLabel("Débit");
		JLabel labelCredit = new JLabel("Crédit");
		JLabel labelPointage = new JLabel("Pointage");
		JLabel labelTaux = new JLabel("Taux");				
		JLabel labelPermanent = new JLabel("Opération référente");
		JLabel labelSolder = new JLabel("Compte à solder");
	
		// Panneau du nom, libellé, tiers, pointage, choix des comptes
		JPanel panelComptes = new JPanel();
		GroupLayout layoutComptes = new GroupLayout(panelComptes);
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
						.addComponent(debit)
						.addComponent(credit)
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
						.addComponent(debit))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelCredit)
						.addComponent(credit))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelPointage)
						.addComponent(pointer)));
	
		// Panneau des opérations proportionnelles
		JPanel propPanel = new JPanel();
		GroupLayout layoutProp = new GroupLayout(propPanel);
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
		transversalPanel.add(cardPane);						//Panneau à la carte
	
		// Panneau des boutons de validation
		JPanel validationPanel = new JPanel();
		validationPanel.setLayout(
				new BoxLayout(validationPanel, BoxLayout.LINE_AXIS));
		
		// Boutons de validation
		JButton valider		= new JButton("Valider");		// Bouton Valider
		JButton appliquer	= new JButton("Appliquer");		// Bouton Appliquer
		JButton supprimer	= new JButton("Supprimer");		// Bouton Supprimer
		JButton quitter		= new JButton("Quitter");		// Bouton Quitter
		valider.addActionListener(EventHandler.create(
				ActionListener.class, this, "applyAndQuit"));
		appliquer.addActionListener(EventHandler.create(
				ActionListener.class, this, "applyAllAndGetSelection"));
		supprimer.addActionListener(EventHandler.create(
				ActionListener.class, this, "confirmAndDelete"));
		quitter.addActionListener(EventHandler.create(
				ActionListener.class, this, "confirmAndQuit"));
		validationPanel.add(supprimer);
		validationPanel.add(Box.createHorizontalGlue());	// Pousser à droite
		validationPanel.add(appliquer);
		validationPanel.add(valider);
		validationPanel.add(quitter);
		
		// Panneau d'édition
		JPanel editionPane = new JPanel();
		editionPane.setLayout(new BoxLayout(editionPane, BoxLayout.PAGE_AXIS));
		editionPane.add(typePanel);							// Choix du type
		editionPane.add(panelComptes);						// Choix du compte
		editionPane.add(transversalPanel);
		editionPane.add(validationPanel);
		
		// Panneau général
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(										// Créer une marge
				BorderFactory.createEmptyBorder(10, 10, 10, 10));
		main.add(scrollList, BorderLayout.WEST);			// Liste Permanents
		main.add(editionPane);								// Panneau d'édition
		return main;
	}

	/**
	 * Configure les boutons radios de choix du type d'opération.
	 */
	private void initTypeButtons() {
		ButtonGroup groupeType = new ButtonGroup();
		// FIXME Revoir la gestion des actions des boutons de type
		// FIXME Si on met les boutons radio en variables d'instance, a-t-on vraiment besoin d'un contrôleur de type ? Il suffit de chercher le bouton sélectionné.
		radioFixe.setActionCommand(FIXE);
		radioProport.setActionCommand(PROPORTIONNEL);
		radioSolder.setActionCommand(SOLDER);
		JRadioButton[] radios = {radioFixe, radioProport, radioSolder};
		for (JRadioButton radio : radios) {			// Pour chaque bouton
			radio.addActionListener(typeController);// Ajouter le Listener
			typeController.buttonMap.put(			// Associer commande/bouton
					radio.getActionCommand(), radio);
			groupeType.add(radio);					// Ajouter au groupe
		}
	}

	/**
	 * Configure la liste déroulante des comptes à solder.
	 * <p>
	 * Cette liste déroulante doit contenir tous les comptes bancaires.<br>
	 * Lorsque l'utilisateur sélectionne un compte dans cette liste, le même
	 * compte doit être sélectionné dans la liste déroulante {@link #debit}.
	 * 
	 * @param comptes	Tous les comptes. Les comptes bancaires seront lus dans
	 * 					ce tableau et insérés dans la liste déroulante dans le
	 * 					même ordre.
	 * 
	 * @see {@link #compteASolder}
	 */
	private void initComptesASolder(Compte[] comptes) {
		
		// Insérer les comptes bancaires
		for (Compte compte : comptes) {
			if (compte.getType().isBancaire()) {
				compteASolder.addItem(compte);
			}
		}
		
		// Répercuter les changements de sélection vers la combo box debit
		compteASolder.addItemListener(EventHandler.create(
				ItemListener.class, debit, "setSelectedItem", "item"));
	}
	
	/**
	 * Remplit la liste graphique avec des contrôleurs de tous les
	 * <code>Permanent</code>s.
	 * 
	 * @param selection	Le <code>Permanent</code> à sélectionner après la mise à
	 * 					jour. Si <code>null</code>, sélectionne l'item
	 * 					"Nouveau..."
	 * 
	 * @throws IOException
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
	 * Applique toutes les modifications et quitte la boîte de dialogue.
	 * 
	 * @throws IOException
	 */
	public void applyAllAndQuit() throws IOException {
		applyAllAndGetSelection();
		quit();
	}
	
	/**
	 * Applique toutes les modifications et recharge les données.
	 * 
	 * @throws IOException
	 */
	public void applyAllAndReload() throws IOException {
		updatePermanentList(applyAllAndGetSelection());
	}
	
	/**
	 * Applique toutes les modifications.
	 * 
	 * @throws IOException
	 */
	private Permanent applyAllAndGetSelection() throws IOException {
		PermanentController selected = dataMediator.getController();
		Permanent selection = null;
		for (PermanentController pc : controllers) {
			try {
				// Appliquer les modifications
				Permanent p = pc.applyChanges();
				
				// Actualiser la sélection si besoin
				if (pc == selected) {
					selection = p;
				}
				
			} catch (IOException e) {
				throw new IOException(
						String.format(
								"Impossible de sauvegarder l'opération%n%s",
								pc),
						e);
			}
		}
		return selection;
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
			} catch (IOException e1) {
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

/**
 * Un contrôleur de <code>Permanent</code>. Permet de pré-visualiser les
 * modifications à apporter à un <code>Permanent</code>.
 * <p>
 * Cette classe est externe à <code>SetupPermanent</code> pour éviter que
 * celle-ci n'accède aux propriétés privés du <code>PermanentController</code>,
 * au lieu de passer par les setters.
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
	private Compte compteASolder = null;
	
	/**
	 * Construit un contrôleur contenant les données actuelles du
	 * <code>Permanent</code> spécifié.
	 * 
	 * @param p	Le <code>Permanent</code> à utiliser. Utiliser <code>null</code>
	 * 			pour la définition d'un nouveau <code>Permanent</code.
	 */
	public PermanentController(Permanent p) {
		permanent = p;								// Mémoriser le Permanent
		refresh();									// Récupérer ses propriétés
	}
	
	/**
	 * Rafraîchit les données à partir des propriétés du <code>Permanent</code>
	 * actuel.
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
			}
			
		} else {									// Pas de Permanent
			type = SetupPermanent.FIXE;				// Type FIXE par défaut
			taux = new BigDecimal("2");				// Taux par défaut
		}
	}
	
	public Permanent getPermanent() {
		return permanent;
	}
	
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
	public Compte getCompteASolder()			{return compteASolder;}
	
	// Setters. Mémoriser l'existence d'une modif.
	public void setType(String type) {
		this.type = type;
		modified = true;
	}
	
	public void setNom(String nom) {
		this.nom = nom;
		modified = true;
	}
	
	public void setDebit(Compte debit) {
		this.debit = debit;
		modified = true;
	}
	
	public void setCredit(Compte credit) {
		this.credit = credit;
		modified = true;
	}
	
	public void setLibelle(String libelle) {
		this.libelle = libelle;
		modified = true;
	}
	
	public void setTiers(String tiers) {
		this.tiers = tiers;
		modified = true;
	}
	
	public void setPointer(boolean pointer) {
		this.pointer = pointer;
		modified = true;
	}
	
	public void setJours(Map<Month,Integer> jours) {
		this.jours = jours;
		modified = true;
	}
	
	public void setMontants(Map<Month,BigDecimal> montants) {
		this.montants = montants;
		modified = true;
	}
	
	public void setDependance(Permanent dependance) {
		this.dependance = dependance;
		modified = true;
	}
	
	public void setTaux(BigDecimal taux) {
		this.taux = taux;
		modified = true;
	}
	
	public void setCompteASolder(Compte compteASolder) {
		this.compteASolder = compteASolder;
		modified = true;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	/**
	 * Applique les modifications au Permanent et envoie la nouvelle version au
	 * DAO.
	 * 
	 * @return	Le nouveau <code>Permanent</code> enregistré dans le DAO. S'il
	 * 			n'y a pas de modifications, c'est la même instance
	 * 			qu'auparavant.
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
					id, nom, debit, credit, libelle, tiers, pointer, jours);
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
	}
	
	/**
	 * Supprime le <code>Permanent</code>. 
	 * 
	 * @throws IOException
	 */
	public void deletePermanent() throws IOException {
		DAOFactory.getFactory().getPermanentDAO().remove(permanent.id);
	}
	
	/**
	 * Applique la relation d'ordre des Permanents au contrôleurs de
	 * <code>Permanent</code>s. Si on contrôleur est vide ("Nouveau..."), il
	 * passe avant.
	 */
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
	}
	
	/**
	 * Affiche le nom du Permanent vers lequel pointe l'objet.
	 */
	@Override
	public String toString() {
		if (permanent == null) {					// Pas de Permanent
			return "Nouveau...";					// Texte pour un nouveau
		} else {
			return permanent.toString();			// Sinon, nom du Permanent
		}
	}
}// package-private class PermanentController

/**
 * Un <code>TableModel</code> pour la table des jours.
 */
@SuppressWarnings("serial")
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
	
	/**
	 * Retourne les données de la ligne spécifiée de la map.
	 * 
	 * @param index	Index de la map interne (et non du modèle).
	 */
	private Entry<Month,Object> getEntryAt(int index) {
		Entry<Month,Object> entry = null;					// La variable
		Iterator<Entry<Month,Object>> iterator =
				map.entrySet().iterator();					// Itérateur
		int i=0;											// Compteur
		while (iterator.hasNext() && i++ <= index) {
			entry = iterator.next();						// Suivant
		}
		return entry;
	}
	
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
				}
			}
		}
		fireTableDataChanged();						// Avertir des changements
	}

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
	}
	
	/**
	 * Renvoie la <code>Map</code> actuelle.
	 */
	public Map<Month,Object> getMap() {
		return map;
	}
	
	/**
	 * Remplace les données actuelles par celles qui sont contenues dans la Map
	 * spécifiée.
	 * 
	 * @param jours	La Map contenant les valeurs à utiliser. L'objet fourni en
	 * 				paramètre n'est pas modifié. Par contrat, cette	Map ne doit
	 * 				contenir comme valeurs que des objets de la	classe fournie
	 * 				au constructeur. Sinon, le comportement est indéfini.
	 */
	public void setMap(Map<Month,? extends Object> jours) {
		map = (jours == null)
				? new TreeMap<>()
				: new TreeMap<>(jours);					// Nouvelles données
		fireTableDataChanged();							// Avertir du changement
	}
}// class PlannerTableModel

/**
 * Un <code>TableCellEditor</code> pour la classe <code>Month</code>.
 */
@SuppressWarnings("serial")
class MonthCellEditor extends DefaultCellEditor {
	
	/**
	 * Construit un éditeur avec <code>JComboBox</code>.
	 */
	public MonthCellEditor() {
		super(new JComboBox<Month>());
	}

	/**
	 * Renvoie une <code>JComboBox</code> pré-remplie avec les 12 mois avant et
	 * après le mois de référence. Si le mois de référence est
	 * <code>null</code>, par rapport à la date du jour.
	 */
	@Override
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
	}
}// class MonthCellEditor