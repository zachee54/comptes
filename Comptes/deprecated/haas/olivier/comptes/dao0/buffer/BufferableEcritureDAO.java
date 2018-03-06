package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.AbstractEcritureDAO;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Interface d'accès aux données permettant un traitement par lots pour les
 * instances Ecriture. Cette interface peut être utilisée avec une sous-couche
 * DAO implémentant un buffer.
 * 
 * @author Olivier HAAS
 */
public interface BufferableEcritureDAO extends AbstractEcritureDAO {

	/**
	 * Ajoute, met à jour et supprime les écritures spécifiées en un seul accès.
	 * Cette méthode nécessite que les ressources en lecture et écriture aient
	 * été préalablement ouvertes.
	 * 
	 * @param add
	 *            Les écritures à ajouter, avec leurs index.
	 * @param update
	 *            Les écritures à mettre à jour, mappés d'après leurs id.
	 * @param remove
	 *            Les identifiants des écritures à supprimer.
	 * @throws IOException
	 */
	void save(Map<Integer, Ecriture> add, Map<Integer, Ecriture> update,
			Set<Integer> remove) throws IOException;
}
