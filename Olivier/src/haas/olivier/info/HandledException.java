/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

/** Une exception qui a déjà été notifiée à l'utilisateur.
 * <p>
 * Elle permet d'éviter que la même erreur ne provoque plusieurs notifications
 * successives à l'utilisateur.
 * <p>
 * L'objet qui intercepte cette exception est informé de l'échec de l'algorithme
 * mais ne doit pas en avertir à nouveau l'utilisateur.
 * <p>
 * Cette classe ne peut être instanciée que par le paquet actuel (constructeur
 * package-private), ce qui assure pratiquement que l'exception a bien été
 * notifiée.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class HandledException extends Exception {

	/** Construit une exception indiquant que l'utilisateur est déjà averti de
	 * l'échec de l'algorithme.
	 * 
	 * @param e	L'exception initiale notifiée à l'utilisateur.
	 */
	HandledException(Throwable e) {
		super(e);
	}// constructeur
}
