package haas.olivier.comptes.dao;

import java.io.IOException;
import java.util.Collection;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

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
	 * 
	 * @param compte	Le compte à ajouter.
	 */
	void add(Compte compte);
	
	/**
	 * Crée un nouveau compte et l'ajoute au modèle.
	 * 
	 * @param type	Le type du nouveau compte.
	 * 
	 * @return		Le nouveau compte créé.
	 */
	Compte createAndAdd(TypeCompte type);
	
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
