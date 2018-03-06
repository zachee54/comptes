package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.AbstractPermanentDAO;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface BufferablePermanentDAO extends AbstractPermanentDAO {

	/**
	 * Ajoute, met à jour et supprime les opérations permanentes spécifiées en
	 * un seul accès. Cette méthode nécessite que les ressources en lecture et
	 * écriture aient été préalablement ouvertes.
	 * 
	 * @param add
	 *            Les opérations permanentes à ajouter, avec leurs index.
	 * @param update
	 *            Les opérations permanentes à mettre à jour, mappés d'après
	 *            leurs id.
	 * @param remove
	 *            Les identifiants des opérations permanentes à supprimer.
	 * @throws IOException
	 */
	void save(Map<Integer, Permanent> add, Map<Integer, Permanent> update,
			Set<Integer> remove) throws IOException;
}
