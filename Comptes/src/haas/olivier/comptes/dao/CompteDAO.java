package haas.olivier.comptes.dao;

import java.io.IOException;
import java.util.Collection;
import haas.olivier.comptes.Compte;

/**
 * L'accès aux comptes dans le modèle de données.
 * 
 * @author Olivier HAAS
 */
public interface CompteDAO {
	
	/**
	 * Renvoie tous les comptes, triés selon leur ordre naturel.
	 */
	Collection<Compte> getAll() throws IOException;
	
	/**
	 * Ajoute un compte.
	 * <p>
	 * Si le compte existe déjà, cette méthode est sans effet.
	 * 
	 * @param compte	Le compte à ajouter.
	 */
	void add(Compte compte);
	
	/**
	 * Supprime un compte.
	 * 
	 * @param compte	Le compte à supprimer.
	 */
	void remove(Compte compte) throws IOException;
	
	/**
	 * Efface tous les comptes.
	 */
	void erase();
}
