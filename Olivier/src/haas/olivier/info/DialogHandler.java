/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

import java.awt.BorderLayout;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Un <code>Handler</code> pour afficher des logs sous la forme de boîtes de
 * dialogues.
 *
 * @author Olivier HAAS
 */
public class DialogHandler extends Handler {
	
	/** Le cadre auquel doivent se rattacher les boîtes de dialogue. */
	private final JFrame frame;
	
	/** Construit un récepteur de messages qui affiche les notifications à
	 * l'écran.
	 * 
	 * @param frame	Le cadre auquel doivent se rattacher les boîtes de dialogue.
	 */
	public DialogHandler(JFrame frame) {
		this.frame = frame;
		setLevel(Level.INFO);
		setFormatter(new DialogFormatter());
	}// constructeur
	
	@Override
	public void publish(final LogRecord record) {
		
		// Vérifier le niveau avant de traiter
		Level level = record.getLevel();
		if (level.intValue() < getLevel().intValue() || level == Level.OFF)
			return;
		
		// Si on n'est pas dans l'EDT, changer de thread
		if (!SwingUtilities.isEventDispatchThread()) {	// Pas dans EDT
			SwingUtilities.invokeLater(new Runnable() {	// Transférer à EDT
				@Override public void run() {
					publish(record);					// Même méthode
				}// run
			});// classe anonyme Runnable
			return;									// C'est tout pour ce thread
		}// if not EDT
		
		// Chercher si une exception est associée à ce log
		Throwable thrown = record.getThrown();			// Exception éventuelle
		String message = getFormatter().format(record);	// Message formaté
		if (thrown != null) {
			error(message, thrown);						// Dialogue "erreur"
		} else {
			dialog(message, record.getLevel());			// Dialogue normal
		}// if thrown
	}// publish
	
	/** Affiche un message d'erreur à l'écran.
	 * 
	 * @param message	Le message d'erreur à afficher.
	 * @param e			L'exception à l'origine de l'erreur.
	 */
	private void error(final String message, final Throwable e) {
		
		// Si on n'est pas dans l'EDT, changer de thread
		if (!SwingUtilities.isEventDispatchThread()) {	// Pas dans EDT
			SwingUtilities.invokeLater(new Runnable() {	// Transférer à EDT
				@Override public void run() {
					error(message, e);					// Même méthode
				}// run
			});// classe anonyme Runnable
			return;									// C'est tout pour ce thread
		}// if not EDT
		
		// Un panneau d'ensemble
		JPanel errorPanel = new JPanel(new BorderLayout());
		
		// Une étiquette contenant le message principal
		JLabel errorLabel = new JLabel();
		errorLabel.setText(message);
		errorLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		errorPanel.add(errorLabel,					// Message simple au-dessus
				BorderLayout.NORTH);
		
		// Une zone de texte contenant le message détaillé
		if (e != null) {
			JTextArea area = new JTextArea(3,40);
			area.setText(							// Définir le texte
					e.getClass().getSimpleName()	// Nom de l'exception
					+ (e.getMessage() == null		// Message, s'il y en a un
							? " "+e.getStackTrace()[0]	// Sinon n° de ligne
							: ": "+ e.getMessage())
					+ (e.getCause() == null			// Cause, s'il y en a une
							? ""
							: " (" + e.getCause().getMessage() + ")"));
			area.setEditable(false);				// Édition interdite
			area.setLineWrap(true);					// Passer à la ligne
			area.setWrapStyleWord(true);			// Sans couper les mots
			area.setFont(errorLabel.getFont());		// Même police que étiquette
			errorPanel.add(new JScrollPane(area));	// Zone de texte au centre
		}// if message détaillé non null
		
		// Afficher la boîte de dialogue
		JOptionPane.showMessageDialog(
				/* Pas de frame si disposed
				 * 
				 * Si le frame parent est déjà fermé, et qu'il n'y a pas d'autre
				 * fenêtre affichée, alors la fermeture du dialog n'entraînera
				 * pas l'arrêt de la JVM alors qu'il le devrait.
				 */
				frame != null && frame.isVisible() ? frame : null,
				errorPanel, "Erreur", JOptionPane.ERROR_MESSAGE);
	}// error
	
	private void dialog(String message, Level level) {
		int niveau = level.intValue();					// Priorité du message
		
		// Titre et type de message par défaut
		String title = null;							// Titre vide
		int messageType = JOptionPane.PLAIN_MESSAGE;	// Message normal
		
		// Titre et type de message selon le niveau de log
		if (niveau >= Level.SEVERE.intValue()) {		// Erreurs
			title = "Erreur";
			messageType = JOptionPane.ERROR_MESSAGE;
			
		} else if (niveau >= Level.WARNING.intValue()) {// Avertissements
			title = "Attention";
			messageType = JOptionPane.WARNING_MESSAGE;
			
		} else if (niveau >= Level.INFO.intValue()) {	// Informations
			title = "Information";
			messageType = JOptionPane.INFORMATION_MESSAGE;
		}
		
		// Afficher le message
		JOptionPane.showMessageDialog(
				frame != null && frame.isVisible() ? frame : null,
				message, title, messageType);
	}// dialog

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}// class DialogHandler

/** Un formateur de message en vue de l'affichage dans une boîte de dialogue.
 * <p>
 * Cette implémentation ajoute des balises HTML et force une largeur de texte de
 * 500, pour permettre un affichage agréable à l'oeil.
 *
 * @author Olivier HAAS
 */
class DialogFormatter extends Formatter {
	
	/** Formate le message spécifié au format HTML avec une largeur de 500. */ 
	@Override
	public String format(LogRecord record) {
		
		// Cas null
		if (record == null)
			return "";
		
		// Cas du message null
		String message = record.getMessage();
		if (message == null)
			return "";
		
		// Cas général
		return "<html><p width=\"500\">" +
				(message.replace("\n", "<br>")) +
				"</p></html>";
	}// format
}
