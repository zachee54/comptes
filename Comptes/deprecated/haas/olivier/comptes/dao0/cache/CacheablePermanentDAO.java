package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.AbstractPermanentDAO;

import java.io.IOException;
import java.util.Set;

/**
 * Interface d'accès aux données permettant une mise en cache des opérations
 * permanentes.
 * 
 * @author Olivier HAAS
 */
public interface CacheablePermanentDAO extends AbstractPermanentDAO {

	/**
	 * Remplace les anciennes données par les données spécifiées.
	 * La sauvegarde physique n'est effectuée qu'après l'invocation de
	 * CacheableDAOFactory.flush().
	 */
	void save(Set<Permanent> permanents) throws IOException;
}
