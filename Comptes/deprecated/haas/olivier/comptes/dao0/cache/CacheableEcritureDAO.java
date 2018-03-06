package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.AbstractEcritureDAO;

import java.io.IOException;
import java.util.TreeSet;

/**
 * Interface d'accès aux données permettant une mise en cache des écritures.
 *
 * @author Olivier HAAS
 */
public interface CacheableEcritureDAO extends AbstractEcritureDAO {

	/**
	 * Remplace les anciennes données par les données spécifiées.
	 * La sauvegarde physique n'est effectuée qu'après l'invocation de
	 * CacheableDAOFactory.flush().
	 * 
	 * @param ecritures
	 * 			Un TreeSet des Ecritures triées par ordre naturel.
	 * @see Ecriture.SortPointage
	 */
	void save(TreeSet<Ecriture> ecritures)
			throws IOException;
}
