package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.AbstractCompteDAO;

import java.io.IOException;
import java.util.Set;

/**
 * Interface d'accès aux données permettant une mise en cache des comptes. 
 * 
 * @author Olivier HAAS
 */
public interface CacheableCompteDAO extends AbstractCompteDAO {
	
	/**
	 * Remplace les anciennes données par les données spécifiées.
	 * La sauvegarde physique n'est effectuée qu'après l'invocation de
	 * CacheableDAOFactory.flush().
	 */
	void save(Set<Compte> comptes) throws IOException;
}
