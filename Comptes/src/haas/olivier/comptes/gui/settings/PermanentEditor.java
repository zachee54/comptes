/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.table.ComptesComboBoxRenderer;
import haas.olivier.comptes.gui.table.FinancialTable;
import haas.olivier.util.Month;

/**
 * Un éditeur graphique des propriétés d'une opération permanente.
 *
 * @author Olivier Haas
 */
class PermanentEditor {
	
	// Constantes de commandes de types de Permanent
	public static final String FIXE = "fixe";
	public static final String PROPORTIONNEL = "proportionnel";
	public static final String SOLDER = "solder";

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
		 * Modifie la vue pour adapter la fenêtre au type sélectionné, et
		 * mémorise ce type dans le <code>dataMediator</code>.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			changeVue(command);
//			dataMediator.getController().setType(command);
		}
	
		/**
		 * Modifie la vue pour adapter la fenêtre au type sélectionné.
		 */
		public void changeVue(String command) {
			AbstractButton bouton;					// Bouton à mettre à jour
			if (!buttonMap.containsKey(command))
				return;
			bouton = buttonMap.get(command);
			
			if (!bouton.isSelected())
				bouton.setSelected(true);
			
			layout.show(panel, command);			// Changer de vue
		}
	}// inner class TypeListener
	
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
	private final JTextComponent champNom = new JTextField();
	
	/**
	 * Champ de saisie du libellé.
	 */
	private final JTextComponent champLibelle = new JTextField();
	
	/**
	 * Champ de saisie du tiers.
	 */
	private final JTextComponent champTiers = new JTextField();
	
	/**
	 * Case à cocher pour le pointage automatique de l'opération.
	 */
	private final JCheckBox checkBoxPointer = new JCheckBox();
	
	/**
	 * Liste déroulante pour choisir le compte débité.
	 */
	private final JComboBox<Compte> comboBoxDebit;
	
	/**
	 * Liste déroulante pour choisir le compte crédité.
	 */
	private final JComboBox<Compte> comboBoxCredit;
	
	/**
	 * Liste déroulante pour choisir l'opération dont dépend celle-ci.
	 */
	private final JComboBox<Permanent> comboBoxDependance = new JComboBox<>();
	
	/**
	 * Champ de saisie du taux, pour une opération dépendante d'une autre.
	 * <p>
	 * Ce champ permet une saisie à la souris avec une précision d'une décimale.
	 */
	private final JSpinner spinnerTaux =
			new JSpinner(new SpinnerNumberModel(0.0, 0.0, null, 0.1));
	
	/**
	 * Planning des jours du mois.
	 */
	private final PlannerTableModel plannerJours =
			new PlannerTableModel(Integer.class, "Jours");
	
	/**
	 * Planning des montants.
	 */
	private final PlannerTableModel plannerMontants =
			new PlannerTableModel(BigDecimal.class, "Montants");

	/**
	 * Construit un éditeur pour les paramètres d'une opération permanente.
	 * 
	 * @throws IOException
	 * 			En cas d'erreur lors de la récupération des comptes.
	 */
	PermanentEditor() throws IOException {
		
		// Configurer les composants de saisie
		Compte[] comptes = getComptes();
		comboBoxDebit = createComptesComboBox(comptes);
		comboBoxCredit = createComptesComboBox(comptes);
		initTypeButtons();
		spinnerTaux.setEditor(new JSpinner.NumberEditor(spinnerTaux, "0.00 '%'"));
		
		// Sélection du type de Permanent (fixe, dépendant ou soldeur)
		JPanel typePanel = createTypePanel();
		typePanel.add(radioFixe);
		typePanel.add(radioProport);
		typePanel.add(radioSolder);
		
		// Les tables de jours et de montants
		JTable tableJours = createPlannerTable(plannerJours);
		JTable tableMontants = createPlannerTable(plannerMontants);
		
		// Étiquettes
		JLabel labelNom = new JLabel("Nom");
		JLabel labelLibelle = new JLabel("Libellé");
		JLabel labelTiers = new JLabel("Tiers");
		JLabel labelDebit = new JLabel("Débit");
		JLabel labelCredit = new JLabel("Crédit");
		JLabel labelPointage = new JLabel("Pointage");
		JLabel labelTaux = new JLabel("Taux");				
		JLabel labelPermanent = new JLabel("Opération référente");
	
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
						.addComponent(champNom)
						.addComponent(champLibelle)
						.addComponent(champTiers)
						.addComponent(comboBoxDebit)
						.addComponent(comboBoxCredit)
						.addComponent(checkBoxPointer)));
		layoutComptes.setVerticalGroup(layoutComptes.createSequentialGroup()
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelNom)
						.addComponent(champNom))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelLibelle)
						.addComponent(champLibelle))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelTiers)
						.addComponent(champTiers))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelDebit)
						.addComponent(comboBoxDebit))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelCredit)
						.addComponent(comboBoxCredit))
				.addGroup(layoutComptes.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelPointage)
						.addComponent(checkBoxPointer)));
	
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
						.addComponent(spinnerTaux)
						.addComponent(comboBoxDependance)));
		layoutProp.setVerticalGroup(layoutProp.createSequentialGroup()
				.addGroup(layoutProp.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelTaux)
						.addComponent(spinnerTaux))
				.addGroup(layoutProp.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelPermanent)
						.addComponent(comboBoxDependance)));
		
		// Les paramètres variables suivant le type d'instance
		JPanel cardPane = typeController.panel;				// Panel à vues
		cardPane.add(new JScrollPane(tableMontants), FIXE);	// Fixes
		cardPane.add(propPanel, PROPORTIONNEL);				// Proportionnels
		cardPane.add(new JPanel(), SOLDER);					// A solder
		
		// Un panneau transversal en deux cases pour les détails
		JPanel transversalPanel = new JPanel(new GridLayout(1,2));
		transversalPanel.add(								// Table des jours
				new JScrollPane(tableJours), BorderLayout.WEST);
		transversalPanel.add(cardPane);						//Panneau à la carte
		
		// Panneau d'édition
		JPanel editionPane = new JPanel();
		editionPane.setLayout(new BoxLayout(editionPane, BoxLayout.PAGE_AXIS));
		editionPane.add(typePanel);							// Choix du type
		editionPane.add(panelComptes);						// Choix du compte
		editionPane.add(transversalPanel);
//		editionPane.add(validationPanel);
		
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
	 * Crée une liste déroulante des comptes.
	 * <p>
	 * Les comptes sont insérés dans la liste déroulante, et l'affichage est
	 * paramétré avec un Renderer approprié.<br>
	 * Si la liste déroulante contient déjà quelque chose, son contenu est
	 * supprimé.
	 * 
	 * @param comptes	Les comptes à insérer dans la liste déroulante.
	 */
	private static JComboBox<Compte> createComptesComboBox(Compte[] comptes) {
		JComboBox<Compte> combo =
				new JComboBox<>(new DefaultComboBoxModel<>(comptes));
		combo.setRenderer(new ComptesComboBoxRenderer());
		return combo;
	}

	/**
	 * Configure les boutons radios de choix du type d'opération.
	 */
	private void initTypeButtons() {
		ButtonGroup groupeType = new ButtonGroup();
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
	 * Renvoie le nom saisi pour l'opération permanente.
	 * 
	 * @return	Le nom saisi pour l'opération permanente.
	 */
	String getNom() {
		return champNom.getText();
	}
	
	/**
	 * Définit le texte à afficher pour le nom de l'opération permanente.
	 */
	void setNom(String nom) {
		champNom.setText(nom);
	}
	
	/**
	 * Renvoie le libellé saisi pour l'opération permanente.
	 * 
	 * @return	Le libellé saisi pour l'opération permanente.
	 */
	String getLibelle() {
		return champLibelle.getText();
	}
	
	/**
	 * Définit le texte à afficher pour le libellé de l'opération permanente.
	 * 
	 * @param libelle	Le texte à afficher pour le libelle dé l'opération
	 * 					permanente.
	 */
	void setLibelle(String libelle) {
		champLibelle.setText(libelle);
	}
	
	/**
	 * Renvoie le nom saisi pour le tiers concerné par l'opération permanente.
	 * 
	 * @return	Le nom saisi pour le tiers concerné par l'opération permanente.
	 */
	String getTiers() {
		return champTiers.getText();
	}
	
	/**
	 * Définit le texte à afficher pour le nom du tiers concerné par l'opération
	 * permanente.
	 * 
	 * @param tiers	Le texte à afficher pour le nom du tiers concerné par
	 * 				l'opération permanente.
	 */
	void setTiers(String tiers) {
		champTiers.setText(tiers);
	}
	
	/**
	 * Indique la valeur saisie pour le pointage automatique de l'opération
	 * permanente.
	 * 
	 * @return	<code>true</code> si l'option de pointage automatique a été
	 * 			saisie (case cochée).
	 */
	boolean isAutoPointee() {
		return checkBoxPointer.isSelected();
	}
	
	/**
	 * Définit l'affichage du pointage automatique de l'opération permanente.
	 * 
	 * @param autoPointee	<code>true</code> s'il faut cocher l'option de
	 * 						pointage automatique.
	 */
	void setAutoPointee(boolean autoPointee) {
		checkBoxPointer.setSelected(autoPointee);
	}
	
	/**
	 * Renvoie le compte sélectionné pour le débit de l'opération permanente.
	 * 
	 * @return	Le compte sélectionné pour le débit de l'opération permanente.
	 */
	Compte getDebit() {
		return comboBoxDebit.getItemAt(comboBoxDebit.getSelectedIndex());
	}
	
	/**
	 * Définit le compte à sélectionner pour le débit de l'opération permanente.
	 * 
	 * @param debit	Le compte à sélectionner pour le débit de l'opération
	 * 				permanente.
	 */
	void setDebit(Compte debit) {
		comboBoxDebit.setSelectedItem(debit);
	}
	
	/**
	 * Renvoie le compte sélectionné pour le crédit de l'opération permanente.
	 * 
	 * @return	Le compte sélectionné pour le crédit de l'opération permanente.
	 */
	Compte getCredit() {
		return comboBoxCredit.getItemAt(comboBoxCredit.getSelectedIndex());
	}
	
	/**
	 * Définit le compte à sélectionner pour le crédit de l'opération
	 * permanente.
	 * 
	 * @param credit	Le compte à sélectionner pour le crédit de l'opération
	 * 					permanente.
	 */
	void setCredit(Compte credit) {
		comboBoxCredit.setSelectedItem(credit);
	}
	
	/**
	 * Renvoie l'opération permanente sélectionnée comme dépendance de
	 * l'actuelle.
	 * 
	 * @return	L'opération permanente sélectionnée comme dépendance de
	 * 			l'actuelle.
	 */
	Permanent getDependance() {
		return comboBoxDependance.getItemAt(
				comboBoxDependance.getSelectedIndex());
	}
	
	/**
	 * Définit l'opération permanente à afficher comme dépendance de celle-ci.
	 * 
	 * @param dependance	L'opération permanente à afficher comme dépendance
	 * 						de celle-ci.
	 */
	void setDependance(Permanent dependance) {
		comboBoxDependance.setSelectedItem(dependance);
	}
	
	/**
	 * Renvoie le taux saisi pour l'opération permanente.
	 * 
	 * @return	Le taux saisi pour l'opération permanente.
	 */
	BigDecimal getTaux() {
		return new BigDecimal(spinnerTaux.getValue().toString());
	}
	
	/**
	 * Définit le taux à afficher pour l'opération permanente.
	 * 
	 * @param taux	Le taux à afficher pour l'opération permanente.
	 */
	void setTaux(BigDecimal taux) {
		spinnerTaux.setValue(taux);
	}
	
	/**
	 * Renvoie le planning des jours saisis pour l'opération permanente.
	 * 
	 * @return	Une <code>Map</code> dont les clés sont les mois et les valeurs
	 * 			les quantièmes des dates prévues pour l'opération permanente.
	 */
	Map<Month, Integer> getJours() {
		return plannerJours.getMap().entrySet().stream().collect(
				Collectors.toMap(Entry::getKey, e -> (Integer) e.getValue()));
	}
	
	/**
	 * Définit le planning des jours à afficher pour l'opération permanente.
	 * 
	 * @param jours	Les jours à afficher pour l'opération permanente.
	 */
	void setJours(Map<Month, Integer> jours) {
		plannerJours.setMap(jours);
	}
	
	/**
	 * Renvoie le planning des montants saisis pour l'opération permanente.
	 * 
	 * @return	Le planning des montants saisis pour l'opération permanente.
	 */
	Map<Month, BigDecimal> getMontants() {
		return plannerMontants.getMap().entrySet().stream().collect(
				Collectors.toMap(
						Entry::getKey, e -> (BigDecimal) e.getValue()));
	}
	
	/**
	 * Définit le planning des montants à afficher pour l'opération permanente.
	 * 
	 * @param montants	Le planning des montants à afficher pour l'opération
	 * 					permanente.
	 */
	void setMontants(Map<Month, BigDecimal> montants) {
		plannerMontants.setMap(montants);
	}
}
