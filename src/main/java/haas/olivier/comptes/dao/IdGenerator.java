/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao;

/** Une classe utilitaire pour générer des identifiants uniques et si possible
 * séquentiels.
 * 
 * @author Olivier HAAS
 */
public class IdGenerator {

	/** L'identifiant le plus élevé parmi ceux utilisés. */
	private int max = -1;
	
	/** Renvoie un nouvel identifiant. */
	public int getId() {
		return ++max;
	}// getId
	
	/** Reçoit un identifiant existant, afin d'éviter que le même identifiant
	 * soit généré une deuxième fois.
	 * 
	 * @param id
	 */
	public void addId(int id) {
		if (id > max)
			max = id;
	}// addId
}
