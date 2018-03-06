package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.dao.AbstractDAO;

import java.io.IOException;
import java.util.Properties;

/**
 * Interface d'accès aux données permettant une mise en cache de l'ensemble des
 * données.
 *
 * @author Olivier HAAS
 */
public interface CacheableDAOFactory extends AbstractDAO {
	
	/** Obtenir un DAO permettant une mise en cache pour les comptes. */
	@Override
	CacheableCompteDAO getCompteDAO();
	
	/** Obtenir un DAO permettant une mise en cache pour les écritures. */
	@Override
	CacheableEcritureDAO getEcritureDAO();
	
	/** Obtenir un DAO permettant une mise en cache pour les opérations
	 * permanentes. */
	@Override
	CacheablePermanentDAO getPermanentDAO();
	
	/** Obtenir un DAO permettant une mise en cache des historiques de comptes.
	 */
	@Override
	CacheableSuiviDAO getHistoriqueDAO();
	
	/** Obtenir un DAO permettant une mise en cache des soldes à vues des
	 * comptes. */
	@Override
	CacheableSuiviDAO getSoldeAVueDAO();
	
	/** Obtenir un DAO permettant une mise en cache des moyennes glissantes des
	 * soldes de comptes. */
	@Override
	CacheableSuiviDAO getMoyenneDAO();
	
	/** Déclenche la sauvegarde physique.
	 * Cette méthode ne doit être appelée qu'après que tous les DAO fabriqués
	 * aient reçu les données à sauvegarder. 
	 * @throws IOException */
	void flush() throws IOException;

	Properties getProperties();
}
