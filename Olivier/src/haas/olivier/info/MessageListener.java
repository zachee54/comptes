/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

/** Un observateur de messages.
 *
 * @author Olivier HAAS
 */
public interface MessageListener {

	/** Traite une erreur. */
	public void error(String message, Throwable throwable);
	
	/** Traite un avertissement. */
	public void warning(String message, Throwable e);
	
	/** Traite un conseil à donner à l'utilisateur. */
	public void conseil(String message);
	
	/** Traite une information. */
	public void info(String message);
}
