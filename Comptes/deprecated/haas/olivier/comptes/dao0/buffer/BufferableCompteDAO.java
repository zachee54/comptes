package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.AbstractCompteDAO;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Interface d'accès aux données permettant un traitement par lots pour les
 * instances Compte. Cette interface peut être utilisée avec une sous-couche DAO
 * implémentant un buffer.
 * 
 * @author Olivier HAAS
 */
public interface BufferableCompteDAO extends AbstractCompteDAO {
	
	/**
	 * Récupère tous les comptes.
	 * L'interface garantit que les comptes déjà instanciés sont récupérés sans
	 * créer de nouvelle instance.
	 * 
	 * @return Un ensemble de tous les comptes ou null si la ressource est
	 *         indisponible.
	 * @throws IOException
	 */
	Set<Compte> getAll() throws IOException;
	
	/**
	 * Ajoute, met à jour et supprime les comptes spécifiés en un seul accès.
	 * Après l'appel à cette méthode, les comptes renvoyés par getAll()
	 * correspondent à de nouvelles instances créées à partir de la relecture
	 * des données sauvegardées.
	 * Cette méthode nécessite que les ressources en lecture et écriture aient
	 * été préalablement ouvertes.
	 * 
	 * @param add
	 *            Les comptes à ajouter, avec leurs index.
	 * @param update
	 *            Les comptes à mettre à jour, mappés d'après leurs id.
	 * @param remove
	 *            Les identifiants des comptes à supprimer
	 * @throws IOException
	 */
	void save(Map<Integer, Compte> add, Map<Integer, Compte> update,
			Set<Integer> remove) throws IOException;
}
