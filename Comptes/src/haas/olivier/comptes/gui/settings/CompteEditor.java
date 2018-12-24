/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import haas.olivier.comptes.TypeCompte;

/**
 * Un éditeur graphique des propriétés d'un compte.
 *
 * @author Olivier Haas
 */
public class CompteEditor {

	/**
	 * Le composant graphique principal.
	 */
	private final Container mainComponent;
	
	/**
	 * Zone de saisie du nom.
	 */
	private final JTextComponent champNom = new JTextField();
	
	/**
	 * Zone de saisie du numéro de compte bancaire.
	 */
	private final JTextComponent champNumero = new JTextField();
	
	/**
	 * Zone de saisie de la date d'ouverture.
	 */
	private final JTextComponent champOuverture = new JTextField();
	
	/**
	 * Zone de saisie de la date de clôture.
	 */
	private final JTextComponent champCloture = new JTextField();
	
	/**
	 * La couleur du compte.
	 */
	private Color compteColor;
	
	/**
	 * Bouton de choix de la couleur.
	 */
	private final JButton colorButton = new JButton(" ");
	
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
	 * Le format de date utilisé par défaut.
	 */
	private final DateFormat defaultDateFormat =
			new SimpleDateFormat("dd/MM/yyyy");

	CompteEditor() {
		JLabel labelNature = new JLabel("Nature :");
		JLabel labelCouleur = new JLabel("Couleur :");
		JLabel labelNom = new JLabel("Nom :");
		JLabel labelOuverture = new JLabel("Ouverture :");
		JLabel labelCloture = new JLabel("Clôture :");
		JLabel labelNumero = new JLabel("Numéro :");
		JLabel labelType = new JLabel("Type :");

		// Composants à n'activer que pour les comptes bancaires
		bancaireComponents.add(labelNumero);
		bancaireComponents.add(champNumero);

		// Panneau de sélection du type principal (bancaire ou budgétaire)
		configureTypeRadioButtons();
		JPanel hautDroite = new JPanel();
		hautDroite.setLayout(new BoxLayout(hautDroite, BoxLayout.PAGE_AXIS));
		hautDroite.setBorder(BorderFactory.createTitledBorder((Border) null));
		hautDroite.add(radioBancaire);
		hautDroite.add(radioBudget);

		// Panneau de couleur pour les diagrammes
		JPanel couleurPanel = new JPanel(new BorderLayout());
		couleurPanel.add(colorButton);
		
		// Le bouton de couleur ouvre un sélecteur de couleur
		colorButton.addActionListener(EventHandler.create(
				ActionListener.class, this, "chooseColor"));

		// Assembler tout
		mainComponent = new JPanel();
		GroupLayout layout = new GroupLayout(mainComponent);
		mainComponent.setLayout(layout);
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
						.addComponent(champNom))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelType)
						.addComponent(typeComboBox))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelOuverture)
						.addComponent(champOuverture))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelCloture)
						.addComponent(champCloture))
				.addGroup(layout.createParallelGroup(
						GroupLayout.Alignment.BASELINE)
						.addComponent(labelNumero)
						.addComponent(champNumero)));
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
						.addComponent(champNom,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(typeComboBox,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(champOuverture,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(champCloture,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(champNumero,
								GroupLayout.DEFAULT_SIZE,
								150,
								GroupLayout.PREFERRED_SIZE)));

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
	 * Adapte la vue pour les comptes bancaires.
	 * <p>
	 * Le bouton radio "Compte bancaire" est sélectionné.
	 * Les composants spécifiques aux comptes bancaires (en l'occurrence le
	 * champ "Numéro" et son étiquette) sont activés.<br>
	 * La liste déroulante des types propose les différents types bancaires.
	 */
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
	 * Renvoie le nom saisi pour le compte.
	 * 
	 * @return	Le nom saisi pour le compte.
	 */
	String getNom() {
		return champNom.getText();
	}
	
	/**
	 * Définit le texte à afficher pour le nom du compte.
	 * 
	 * @param nom	Le texte à afficher pour le nom du compte.
	 */
	void setNom(String nom) {
		champNom.setText(nom);
	}
	
	/**
	 * Renvoie le numéro saisi pour le compte.
	 * 
	 * @return	Le numéro saisi pour le compte, ou <code>null</code> si le
	 * 			numéro saisi est invalide.
	 */
	Long getNumero() {
		try {
			return Long.parseLong(champNumero.getText());
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Définit le numéro à afficher pour le compte.
	 * 
	 * @param numero	Le numéro à afficher pour le compte.
	 */
	void setNumero(Long numero) {
		champNumero.setText(numero == null ? "" : numero.toString());
	}
	
	/**
	 * Renvoie la date d'ouverture saisie.
	 * 
	 * @return	La date d'ouverture saisie, ou <code>null</code> si la zone de
	 * 			saisie est vide.
	 * 
	 * @throws ParseException
	 * 			Si le texte n'est pas vide mais ne correspond pas à une date au
	 * 			format <code>jj/MM/aa</code> ni <code>jj/MM/aaaa</code>.
	 */
	Date getOuverture() throws ParseException {
		return parseDate(champOuverture.getText());
	}
	
	/**
	 * Définit la date d'ouverture à afficher pour le compte.
	 * 
	 * @param ouverture	La date d'ouverture à afficher pour le compte, ou
	 * 					<code>null</code> s'il ne faut rien afficher.
	 */
	void setOuverture(Date ouverture) {
		champOuverture.setText(ouverture == null
				? ""
				: defaultDateFormat.format(ouverture));
	}
	
	/**
	 * Renvoie la date de clôture saisie.
	 * 
	 * @return	La date de clôture saisie, ou <code>null</code> si la zone de
	 * 			saisie est vide.
	 * 
	 * @throws ParseException
	 * 			Si le texte n'est pas vide mais ne correspond pas à une date au
	 * 			format <code>jj/MM/aa</code> ni <code>jj/MM/aaaa</code>.
	 */
	Date getCloture() throws ParseException {
		return parseDate(champCloture.getText());
	}
	
	/**
	 * Définit la date de clôture à afficher pour le compte.
	 * 
	 * @param cloture	La date de clôture à afficher pour le compte, ou
	 * 					<code>null</code> s'il ne faut rien afficher.
	 */
	void setCloture(Date cloture) {
		champCloture.setText(cloture == null
				? ""
				: defaultDateFormat.format(cloture));
	}

	/**
	 * Parse une date au format <code>jj/MM/aa</code> ou
	 * <code>jj/MM/aaaa</code>.
	 * 
	 * @param text	Le texte à parser.
	 * @return		La date représentée par ce texte, ou <code>null</code> si le
	 * 				texte est vide.
	 * 
	 * @throws ParseException
	 * 				Si le texte n'est pas vide mais ne correspond pas à une date
	 * 				au format <code>jj/MM/aa</code> ni <code>jj/MM/aaaa</code>.
	 */
	private Date parseDate(String text) throws ParseException {
		if (text.isEmpty())
			return null;
		
		DateFormat dateFormat = (text.length() < 10)
				? new SimpleDateFormat("dd/MM/yy")
				: defaultDateFormat;
		return dateFormat.parse(text);
	}
	
	/**
	 * Renvoie la couleur du compte.
	 * 
	 * @return	La couleur du compte, ou <code>null</code> si elle n'est pas
	 * 			définie.
	 */
	Color getColor() {
		return compteColor;
	}
	
	/**
	 * Définit la couleur à afficher.
	 * 
	 * @param color	La couleur à afficher.
	 */
	void setColor(Color color) {
		compteColor = color;
		colorButton.setBackground(color);
	}
	
	/**
	 * Ouvre un sélecteur de couleur permettant à l'utilisateur de modifier la
	 * couleur sélectionnée.
	 */
	public void chooseColor() {
		Color color = JColorChooser.showDialog(mainComponent,
				"Choisissez une couleur", getColor());
		if (color != null)
			setColor(color);
	}
	
	/**
	 * Renvoie le type de compte sélectionné.
	 * 
	 * @return	Le type de compte sélectionné.
	 */
	TypeCompte getTypeCompte() {
		return typeComboBox.getItemAt(typeComboBox.getSelectedIndex());
	}
	
	/**
	 * Définit le type de compte.
	 * 
	 * @param type Le type de compte.
	 */
	void setTypeCompte(TypeCompte type) {
		if (type.isBancaire()) {
			setVueBancaire();
		} else if (type.isBudgetaire()) {
			setVueBudget();
		}
		typeComboBox.setSelectedItem(type);
	}
	
	/**
	 * Renvoie le composant graphique de l'éditeur.
	 * 
	 * @return	Le composant graphique de l'éditeur.
	 */
	Component getComponent() {
		return mainComponent;
	}
}
