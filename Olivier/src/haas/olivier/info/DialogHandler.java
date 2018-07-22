/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

import java.awt.BorderLayout;
import java.awt.Component;
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

/**
 * Un <code>Handler</code> pour afficher des logs sous la forme de boîtes de
 * dialogue.
 *
 * @author Olivier HAAS
 */
public class DialogHandler extends Handler {
	
	/**
	 * Le cadre auquel doivent se rattacher les boîtes de dialogue.
	 */
	private final JFrame frame;
	
	/**
	 * Construit un récepteur de messages qui affiche les notifications à
	 * l'écran.
	 * 
	 * @param frame	Le cadre auquel doivent se rattacher les boîtes de dialogue.
	 */
	public DialogHandler(JFrame frame) {
		this.frame = frame;
		setLevel(Level.INFO);
		setFormatter(new DialogFormatter());
	}
	
	@Override
	public void publish(final LogRecord record) {
		
		// Vérifier le niveau avant de traiter
		if (!checkLevel(record)) {
			return;
		}
		
		// Si on n'est pas dans l'EDT, changer de thread
		if (!SwingUtilities.isEventDispatchThread()) {
			publishOnEventDispatchThread(record);
			return;										// Fini pour ce thread
		}
		
		// Afficher une boîte de dialogue "erreur" ou "normale" selon le cas
		Throwable thrown = record.getThrown();
		String message = getFormatter().format(record);
		if (thrown != null) {
			error(message, thrown);
		} else {
			dialog(message, record.getLevel());
		}
	}
	
	/**
	 * Indique si le niveau du message de log justifie de le traiter.
	 *
	 * @param record	Le message de log à examiner.
	 * @return			<code>true</code> si le niveau de <code>record</code>
	 * 					fait qu'il doit être traité.
	 */
	private boolean checkLevel(LogRecord record) {
		Level level = record.getLevel();
		return (level != Level.OFF)
				&& (level.intValue() >= getLevel().intValue());
	}
	
	/**
	 * Pulie le message de log sur l'EDT.
	 *
	 * @param record	Le message de log à publier.
	 */
	private void publishOnEventDispatchThread(final LogRecord record) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				publish(record);
			}
		});
	}
	
	/**
	 * Affiche un message d'erreur décrivant une exception.
	 * 
	 * @param message	Le message d'erreur à afficher.
	 * @param e			L'exception à l'origine de l'erreur.
	 */
	private void error(String message, Throwable e) {
		
		// Un panneau d'ensemble
		JPanel errorPanel = new JPanel(new BorderLayout());
		
		// Une étiquette contenant le message principal au-dessus
		JLabel errorLabel = new JLabel(message);
		errorLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		errorPanel.add(errorLabel, BorderLayout.NORTH);
		
		// Une zone de texte contenant le message détaillé
		if (e != null) {
			errorPanel.add(new JScrollPane(createExceptionDescriptionArea(e)));
		}
		
		// Afficher la boîte de dialogue
		JOptionPane.showMessageDialog(getParentFrame(), errorPanel, "Erreur",
				JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Crée un composant décrivant l'exception et sa cause.
	 *
	 * @param e	L'exception à décrire.
	 * @return	Une zone de texte contenant la description de l'exception et de
	 * 			sa cause.
	 */
	private Component createExceptionDescriptionArea(Throwable e) {
		JTextArea area = new JTextArea(3, 40);
		String description = String.format(
				"%s: %s (%s)",
				e.getClass().getSimpleName(),		// Nom de l'exception
				(e.getMessage() == null)			// Message, s'il y en a un
						? e.getStackTrace()[0]		// Sinon n° de ligne
						: e.getMessage(),
				(e.getCause() == null)				// Cause, s'il y en a une
						? ""
						: e.getCause().getMessage());
		area.setText(description);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		if (frame != null)
			area.setFont(frame.getFont());			// Même police que frame
		return area;
	}
	
	/**
	 * Renvoie le composant qui doit servir de parent à la boîte de dialogue.
	 * <p>
	 * Si {@link #frame} est déjà fermé et qu'il n'y a pas d'autre fenêtre
	 * affichée, alors la fermeture du dialog n'entraînera pas l'arrêt de la
	 * JVM alors qu'il le devrait.<br>
	 * La méthode renvoie donc <code>null</code> si <code>frame</code> n'est pas
	 * affiché.
	 *
	 * @return	<code>frame</code> s'il est affiché, sinon <code>null</code>.
	 */
	private JFrame getParentFrame() {
		return ((frame != null) && frame.isVisible()) ? frame : null;
	}
	
	/**
	 * Affiche une boîte de dialogue.
	 *
	 * @param message	Le texte à afficher.
	 * @param level		Le niveau de log, déterminant le type de boîte qui sera
	 * 					affichée.
	 */
	private void dialog(String message, Level level) {
		int niveau = level.intValue();
		
		// Titre et type de message par défaut
		String title = null;
		int messageType = JOptionPane.PLAIN_MESSAGE;
		
		// Titre et type de message selon le niveau de log
		if (niveau >= Level.SEVERE.intValue()) {
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
		JOptionPane.showMessageDialog(getParentFrame(), message, title,
				messageType);
	}

	@Override
	public void flush() {
		// Pas de données en attente
	}

	@Override
	public void close() {
		// Aucune ressource à fermer 
	}
}
