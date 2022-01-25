package haas.olivier.comptes.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.mysql.MySqlDAO;
import haas.olivier.comptes.gui.SimpleGUI;

public class SetupSqlDAO {
	
	/** Le Logger de cette classe. */
	private static final Logger LOGGER =
			Logger.getLogger(SetupSqlDAO.class.getName());

	/**
	 * Crée et lance une boîte de dialogue pour ouvrir une base de données.
	 * 
	 * @param gui	L'interface principale.
	 */
	public static void runOpenDialog(SimpleGUI gui) {
		SetupSqlDAO instance = new SetupSqlDAO(gui);
		instance.init("Ouvrir une base de données", "openDatabase");
		instance.showDialog();
	}
	
	/**
	 * Crée et lance une boîte de dialogue pour sauvegarder les données dans une
	 * base de données.
	 * 
	 * @param gui	L'interface principale.
	 */
	public static void runSaveDialog(SimpleGUI gui) {
		SetupSqlDAO instance = new SetupSqlDAO(gui);
		instance.init("Sauvegarder dans une base de données", "saveDatabase");
		instance.showDialog();
	}
	
	/** L'interface principale de l'application. */
	private final SimpleGUI gui;
	
	/** La boîte de dialogue d'ouverture de la ressource. */
	private final JDialog dialog;

	/** Le champ du nom de l'hôte. */
	private final JTextField hostField = new JTextField("localhost");
	
	/** Le champ du numéro de port. */
	private final JTextField portField = new JTextField("3306");
	
	/** Le champ du nom de la base de données. */
	private final JTextField databaseField = new JTextField("comptes");
	
	/** Le champ du nom d'utilisateur. */
	private final JTextField usernameField = new JTextField("comptes");
	
	/** Le champ du mot de passe. */
	private final JTextField passwordField = new JPasswordField();
	
	/**
	 * Construit une boîte dialogue de sélection d'une base de données.
	 * 
	 * @param gui			L'interface principale.
	 */
	private SetupSqlDAO(SimpleGUI gui) {
		this.gui = gui;
		this.dialog = new JDialog(gui.getFrame(), true);
	}
	
	/**
	 * Fabrique le contenu de la boîte de dialogue.
	 * 
	 * @param title			Le titre de la boîte de dialogue.
	 * @param actionMethod	Le nom de la méthode de l'objet actuel, à lancer
	 * 						lors de la validation de la boîte de dialogue.
	 */
	private void init(String title, String actionMethod) {
		dialog.setTitle(title);
		
		JLabel hostLabel = new JLabel("Hôte");
		JLabel portLabel = new JLabel("Port");
		JLabel databaseLabel = new JLabel("Nom de la base");
		JLabel usernameLabel = new JLabel("Utilisateur");
		JLabel passwordLabel = new JLabel("Mot de passe");
		
		// Fixer la largeur d'un grand élément (les autres suivent)
		hostField.setPreferredSize(new Dimension(
				150,
				hostField.getPreferredSize().height));
		
		// Fixer la largeur du portField, plus petit que les autres
		portField.setPreferredSize(new Dimension(
				50,
				portField.getPreferredSize().height));
		
		JPanel content = new JPanel();
		GroupLayout groupLayout = new GroupLayout(content);
		content.setLayout(groupLayout);
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(hostLabel)
						.addComponent(portLabel)
						.addComponent(databaseLabel)
						.addComponent(usernameLabel)
						.addComponent(passwordLabel))
				.addGroup(groupLayout.createParallelGroup(
						Alignment.LEADING, false)
						.addComponent(hostField)
						.addComponent(portField,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(databaseField)
						.addComponent(usernameField)
						.addComponent(passwordField)));
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(hostLabel)
						.addComponent(hostField))
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(portLabel)
						.addComponent(portField))
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(databaseLabel)
						.addComponent(databaseField))
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(usernameLabel)
						.addComponent(usernameField))
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(passwordLabel)
						.addComponent(passwordField)));
		
		dialog.add(content);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(EventHandler.create(
				ActionListener.class, this, actionMethod));
		
		JButton cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(EventHandler.create(
				ActionListener.class, dialog, "dispose"));
		
		JPanel submitPanel = new JPanel(new GridLayout());
		submitPanel.add(okButton);
		submitPanel.add(cancelButton);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(submitPanel);
		dialog.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Affiche la boîte de dialogue.
	 */
	private void showDialog() {
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getOwner());
		dialog.setVisible(true);
	}
	
	/**
	 * Ouvre la base de données en utilisant les données saisies.
	 */
	public void openDatabase() {
		try {
			toggleFactory(false);
			dialog.dispose();
			gui.createTabs();
			
		} catch (IOException e) {
			LOGGER.log(
					Level.SEVERE, "Impossible d'ouvrir la base de données", e);
		}
	}
	
	/**
	 * Sauvegarde les données actuelles dans une base de données en utilisant
	 * les données saisies.
	 */
	public void saveDatabase() {
		try {
			toggleFactory(true);
			dialog.dispose();
			DAOFactory.getFactory().save();
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Impossible de sauvegarder la base de données", e);
		}
	}
	
	/**
	 * Bascule vers une nouvelle MySqlDAOFactory définie d'après les données
	 * saisies.
	 * 
	 * @param replace	Si <code>true</code>, les données sont trsnaférées
	 * 					depuis l'ancienne Factory.
	 * 
	 * @throws IOException
	 */
	private void toggleFactory(boolean replace) throws IOException {
		try {
			DAOFactory.setFactory(
					new CacheDAOFactory(new MySqlDAO(
							hostField.getText(),
							Integer.parseInt(portField.getText()),
							databaseField.getText(),
							usernameField.getText(),
							passwordField.getText())),
					replace);
			gui.updateDaoName();
			
		} catch (NumberFormatException e) {
			throw new IOException(
					"Le numéro de port doit être un nombre entier", e);
		}
	}
}
