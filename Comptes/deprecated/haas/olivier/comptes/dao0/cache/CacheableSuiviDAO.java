package haas.olivier.comptes.dao0.cache;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.AbstractSuiviDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface d'accès aux données permettant une mise en cache des suivis de
 * comptes.
 *
 * @author Olivier HAAS
 */
public interface CacheableSuiviDAO extends AbstractSuiviDAO {

	/**
	 * Remplace les anciennes données par les données spécifiées.
	 * La sauvegarde physique n'est effectuée qu'après l'invocation de
	 * CacheableDAOFactory.flush().
	 */
	void save(Map<Month, Map<Integer, BigDecimal>> suivis) throws IOException;
}
