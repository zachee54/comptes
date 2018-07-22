package haas.olivier.comptes.info;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Un intercepteur d'exceptions qui renvoie vers le système de logs.
 * <p>
 * Cette classe permet d'afficher à l'écran les exceptions non traitées, plutôt
 * que sur la console. Utile pour les programmes graphiques, puisque la console
 * n'est pas visible !
 *
 * @author Olivier HAAS
 */
public class UncaughtExceptionLogger implements UncaughtExceptionHandler {

	/**
	 * Le Logger pour cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(UncaughtExceptionLogger.class.getName());
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.log(Level.SEVERE, "Erreur à l'exécution", e);
	}

}
