/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

/** Une exception levée lorsque les arguments fournis sont incohérents.
 * <p>
 * Cette exception est utilisée lors de l'instanciation des
 * <code>Ecriture</code>.
 *
 * @author Olivier HAAS
 */
public class InconsistentArgumentsException extends Exception {
	private static final long serialVersionUID = -6360972273813949464L;

	/** Construit une exception indiquant une incohérence entre différents
	 * arguments spécifiés.
	 * 
	 * @param message	Le message de l'exception.
	 */
	public InconsistentArgumentsException(String message) {
		super(message);
	}// constructeur
}
