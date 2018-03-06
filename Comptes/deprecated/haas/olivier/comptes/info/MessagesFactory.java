package haas.olivier.comptes.info;

/**
 * Fabrique de gestionnaire de messages Les gestionnaires de messages permettent
 * d'afficher des boîtes de dialogue pour avertir l'utilisateur (erreur,
 * information, question, etc). Cette fabrique utilise par défaut un
 * gestionnaire qui imprime sur la sortie standard.
 * 
 * @author Olivier HAAS
 */
public class MessagesFactory {

	// Instance en cours
	protected static MessagesFactory factory = null;

	/**
	 * Renvoie l'instance en cours, ou une instance par défaut qui imprime sur
	 * la sortie standard
	 */
	public static MessagesFactory getInstance() {
		if (factory == null) {
			factory = new MessagesFactory();
		}
		return factory;
	}

	/**
	 * Remplace l'instance actuelle par celle qui est spécifiée.
	 * 
	 * @param f
	 *            La nouvelle instance
	 */
	public static void setFactory(MessagesFactory f) {
		factory = f;
	}

	// Constructeur protégé
	protected MessagesFactory() {
	}

	/**
	 * Affiche un message d'erreur.
	 * 
	 * @param message
	 *            Le texte du message à afficher.
	 */
	public void showErrorMessage(String message) {
		System.err.println("Erreur: " + message);
	}

	/**
	 * Affiche un message d'information.
	 * 
	 * @param message
	 *            Le textes du message à afficher.
	 */
	public void showInformationMessage(String message) {
		System.out.println(message);
	}
}
