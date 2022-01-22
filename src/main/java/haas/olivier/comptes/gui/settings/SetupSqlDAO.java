package haas.olivier.comptes.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.mysql.MySqlDAO;

public class SetupSqlDAO {

	public static void runOpenDialog(JFrame frame) {
		SetupSqlDAO instance = new SetupSqlDAO(frame);
		instance.showOpenDialog();
	}
	
	/** La boîte de dialogue d'ouverture de la ressource. */
	private final JDialog dialog;

	/** Le champ du nom de l'hôte. */
	private final JTextField hostField = new JTextField();
	
	/** Le champ du numéro de port. */
	private final JTextField portField = new JTextField();
	
	/** Le champ du nom de la base de données. */
	private final JTextField databaseField = new JTextField();
	
	/** Le champ du nom d'utilisateur. */
	private final JTextField usernameField = new JTextField();
	
	/** Le champ du mot de passe. */
	private final JTextField passwordField = new JPasswordField();
	
	private SetupSqlDAO(JFrame frame) {
		this.dialog = new JDialog(frame, true);
		
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
				ActionListener.class, this, "openDatabase"));
		
		JButton cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(EventHandler.create(
				ActionListener.class, dialog, "dispose"));
		
		JPanel submitPanel = new JPanel();
		submitPanel.add(okButton);
		submitPanel.add(cancelButton);
		dialog.add(submitPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Affiche la boîte de dialogue.
	 */
	private void showOpenDialog() {
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getOwner());
		dialog.setVisible(true);
	}
	
	/**
	 * Ouvre la base de données utilisant les données saisies.
	 */
	public void openDatabase() {
		Logger logger = Logger.getLogger(this.getClass().getName());
		
		try {
			DAOFactory.setFactory(
					new CacheDAOFactory(new MySqlDAO(
							hostField.getText(),
							Integer.parseInt(portField.getText()),
							databaseField.getText(),
							usernameField.getText(),
							passwordField.getText())));
			
		} catch (NumberFormatException e) {
			logger.severe("Le numéro de port doit être un nombre entier");
			
		} catch (IOException e) {
			logger.log(
					Level.SEVERE, "Impossible d'ouvrir la base de données", e);
		}
	}
}
