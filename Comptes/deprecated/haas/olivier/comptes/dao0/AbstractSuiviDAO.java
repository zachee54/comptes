package haas.olivier.comptes.dao0;

import haas.olivier.util.Month;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface générique d'accès aux données pour les suivis de comptes.
 * Il s'agit des méthodes que tous les DAO de suivis des comptes doivent
 * implémenter par principe.
 * Cette interface est "Abstract" dans le sens où elle n'est pas faite pour être
 * implémentée seule. 
 *
 * @author Olivier HAAS
 */
public interface AbstractSuiviDAO {
	
	/**
	 * Renvoie tous les montants.
	 * @return	Une multimap contenant les mois et l'id de chaque compte.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	Map<Month, Map<Integer, BigDecimal>> getAll() throws IOException;

	/** Supprime un compte du fichier de suivi */
	void removeSuiviCompte(int id) throws IOException;
}
