package haas.olivier.info;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Un récepteur de messages qui affiche les notifications à l'écran.
 * 
 * @author Olivier HAAS
 */
public class DialogMessageListener implements MessageListener {

	/** Le cadre auquel doivent se rattacher les boîtes de dialogue. */
	private JFrame frame;
	
	/** L'étiquette affichant les informations dans le cadre graphique de
	 * l'application. */
	private JLabel infoLabel;
	
	/** Construit un récepteur de messages qui affiche les notifications à
	 * l'écran.
	 * 
	 * @param frame	Le cadre auquel doivent se rattacher les boîtes de dialogue.
	 */
	public DialogMessageListener(JFrame frame, JLabel infoLabel) {
		this.frame = frame;
		this.infoLabel = infoLabel;
	}// constructeur
	
	/** Affiche un message d'erreur à l'écran. */
	@Override
	public void error(final String message, final Throwable e) {
		
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
		errorLabel.setText(formatMessage(message));
		errorLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		errorPanel.add(errorLabel,				// Message simple au-dessus
				BorderLayout.NORTH);
		
		// Une zone de texte contenant le message détaillé
		if (e != null) {
			JTextArea area = new JTextArea(3,40);
			area.setText(						// Définir le texte
					e.getClass().getName() + ": "//Nom de la classe d'exception
					+ (e.getMessage() == null	// Message, s'il y en a un
							? e.getStackTrace()[0]	// Sinon n° de ligne source
							: e.getMessage())
					+ (e.getCause() == null		// Cause, s'il y en a une
							? ""
							: " (" + e.getCause().getMessage() + ")"));
			area.setEditable(false);			// Édition interdite
			area.setLineWrap(true);				// Passer à la ligne
			area.setWrapStyleWord(true);		// Sans couper les mots
			area.setFont(errorLabel.getFont());	// Même police que l'étiquette
			errorPanel.add(new JScrollPane(area));//Zone de texte au centre
		}// if message détaillé non null
		
		// Afficher la boîte de dialogue
		JOptionPane.showMessageDialog(
				frame, errorPanel, "Erreur", JOptionPane.ERROR_MESSAGE);
	}// error

	/** Affiche un avertissement à l'écran. */
	@Override
	public void warning(final String message, final Throwable e) {
		
		// Si on n'est pas dans l'EDT, changer de thread
		if (!SwingUtilities.isEventDispatchThread()) {	// Pas dans EDT
			SwingUtilities.invokeLater(new Runnable() {	// Transférer à EDT
				@Override public void run() {
					warning(message, e);				// Même méthode
				}// run
			});// classe anonyme Runnable
			return;									// C'est tout pour ce thread
		}// if not EDT
		
		JOptionPane.showMessageDialog(
				frame,
				formatMessage(message),
				"Attention",
				JOptionPane.WARNING_MESSAGE);
	}// warning
	
	/** Affiche un message d'information à l'écran. */
	@Override
	public void conseil(final String message) {
		
		// Si on n'est pas dans l'EDT, changer de thread
		if (!SwingUtilities.isEventDispatchThread()) {	// Pas dans EDT
			SwingUtilities.invokeLater(new Runnable() {	// Transférer à EDT
				@Override public void run() {
					conseil(message);					// Même méthode
				}// run
			});// classe anonyme Runnable
			return;									// C'est tout pour ce thread
		}// if not EDT
		
		JOptionPane.showMessageDialog(
				frame,
				formatMessage(message),
				"Conseil",
				JOptionPane.INFORMATION_MESSAGE);
	}// conseil

	/** Affiche l'information dans un <code>JLabel</code> pré-défini. */
	@Override
	public void info(final String message) {
		
		// Si on n'est pas dans l'EDT, changer de thread
		if (!SwingUtilities.isEventDispatchThread()) {	// Pas dans EDT
			SwingUtilities.invokeLater(new Runnable() {	// Transférer à EDT
				@Override public void run() {
					info(message);						// Même méthode
				}// run
			});// classe anonyme Runnable
			return;									// C'est tout pour ce thread
		}// if not EDT
		
		if (infoLabel != null)
			infoLabel.setText(formatMessage(message));
	}// info
	
	/** Formate le message spécifié au format HTML avec une largeur de 500. */ 
	private String formatMessage(String message) {
		if (message == null)
			return "";
		
		return "<html><p width=\"500\">" +
				(message.replace("\n", "<br>")) +
				"</p></html>";
	}// formatMessage
}
