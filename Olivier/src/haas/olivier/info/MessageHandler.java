/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Une classe de réception et de diffusion de messages.
 * Pattern Observable
 * 
 * @author Olivier HAAS
 */
public class MessageHandler {
	
	/** Le logger interne de la classe. */
	private static final Logger LOGGER =
			Logger.getLogger(MessageHandler.class.getName());

	/** Les observateurs d'erreur. */
	private static Set<MessageListener> listeners =
			new HashSet<MessageListener>();
	
	/** Ajoute un observateur de messages. */
	public static void addMessageListener(MessageListener l) {
		listeners.add(l);
	}
	
	/** Enlève un observateur de messages. */
	public static void removeMessageListener(MessageListener l) {
		listeners.remove(l);
	}
	
	/** Notifie les observateurs de l'apparition d'une erreur.
	 * 
	 * @param message	Le message à donner à l'utilisateur.
	 * 
	 * @param throwable	L'exception à l'origine de l'erreur.
	 * 
	 * @return			Cette méthode lève systématiquement une
	 * 					<code>HandledException</code>. Comme elle ne se termine
	 * 					jamais normalement, elle ne renvoie donc jamais aucune
	 * 					valeur. Mais on déclare quand même une valeur de retour
	 * 					sous un type générique pour contourner les contrôles du
	 * 					compilateur.<br>
	 * 					En effet, dans une méthode qui est censée renvoyer
	 * 					quelque chose, la survenance d'une erreur qui justifie
	 * 					l'appel à <code>notifyError()</code> arrête la méthode
	 * 					et renvoie une <code>HandledException</code>, mais le
	 * 					compilateur n'est pas satisfait parce qu'il veut une
	 * 					valeur de retour (il ne sait pas que
	 * 					<code>notifyError()</code> va lever une exception).<br>
	 * 					En écrivant
	 * 					<code>return MessageHandler.notifyError(...)</code>, on
	 * 					fait croire au compilateur qu'on va renvoyer quelque
	 * 					chose et cela lui suffit. Le type générique de la
	 * 					méthode, appelé de manière implicite, autorise le
	 * 					compilateur à y voir n'importe quel type (mais en
	 * 					l'occurrence, il y verra celui qu'il attend).
	 * 
	 * @throws HandledException
	 * 					Dans tous les cas, la méthode lève une
	 * 					<code>HandledException</code> qui permet de transmettre
	 * 					l'exception initiale à toute la pile, tout en indiquant
	 * 					qu'elle a déjà été notifiée à l'utilisateur.
	 */
	static <T> T notifyError(String message, Throwable throwable)
			throws HandledException {
		
		// API Logger
		LOGGER.log(Level.SEVERE, message, throwable);
		
		// Log interne
		for (MessageListener l : listeners) {
			l.error(message, throwable);			// Notifier les listeners
		}
		notifyInfo("Erreur");						// Remplacer texte d'info
		
		// Transmettre l'exception "traitée"
		throw new HandledException(throwable);
	}// notifyError
	
	/** Notifie les observateurs d'un avertissement.
	 * 
	 * @param message	Le message à donner à l'utilisateur.
	 * @param e			L'exception à l'origine de l'erreur.
	 */
	public static void notifyWarning(String message, Throwable e) {
		for (MessageListener l : listeners) {
			l.warning(message, e);
		}
	}// notifyWarning
	
	/** Notifie un conseil à donner à l'utilisateur. */
	public static void notifyConseil(String message) {
		for (MessageListener l : listeners) {
			l.conseil(message);
		}
	}// notifyConseil
	
	/** Notifie une information aux observateurs.
	 * 
	 * @param message	L'information à transmettre à l'utilisateur, ou
	 * 					<code>null</code> pour indiquer un message par défaut.
	 * 					(en l'occurrence, le message par défaut est "Prêt", pour
	 * 					indiquer que les traitements sont finis).
	 */
	public static void notifyInfo(String message) {
		
		// Message par défaut
		if (message == null) message = "Prêt";
		
		// Notifier aux observateurs
		for (MessageListener l : listeners) {
			l.info(message);
		}
	}// notifyInfo
}
