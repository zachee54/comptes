package haas.olivier.comptes.dao0.cache;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.comptes.dao.util.CacheSuivi;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Classe d'accès aux données de suivi, implémentant un cache.
 * Toutes les données (historiques, soldes à vue, moyennes...) sont chargées
 * dans un cache. Lors de la sauvegarde, la totalité du cache est sauvegardée
 * dans le CacheableSuiviDAO sous-jacent en écrasant toutes les anciennes
 * données.
 *
 * @author Olivier HAAS
 */
public class CacheSuiviDAO implements SuiviDAO {

	/** Sous-couche DAO. */
	private CacheableSuiviDAO dao;
	
	/** Le cache. */
	private CacheSuivi cache;
	
	/** Indicateur de changement de données. */
	boolean mustBeSaved = false;
	
	/** Construit un CacheSuiviDAO pointant vers le CacheablSuiviDAO spécifié.
	 * Les données sont chargées dès la création de l'objet.
	 */
	public CacheSuiviDAO(CacheableSuiviDAO dao) {
		this.dao = dao;
		cache = new CacheSuivi(dao);
	}
	
	@Override
	public Map<Month, Map<Integer, BigDecimal>> getAll() throws IOException {
		return cache.getCache();
	}

	@Override
	public void removeSuiviCompte(int id) throws IOException {
		
		// Parcourir les mois
		for (Map<Integer,BigDecimal> map : cache.getCache().values()) {
			
			// Parcourir les comptes
			Iterator<Integer> it = map.keySet().iterator();
			while (it.hasNext()) {
				if (it.next().intValue() == id) {	// C'est ce compte ?
					it.remove();					// Supprimer l'occurrence
					mustBeSaved = true;				// Signaler le changement
				}// if id
			}// while comptes
		}// for mois
	}// removeSuiviCompte

	@Override
	public BigDecimal get(int id, Month month) {
		try {
			return cache.getCache().get(month).get(id);//Valeur dans la multimap
		} catch (NullPointerException e) {				// Il n'y a pas ce mois?
			return null;									// null
		} catch (IOException e) {						// Autre erreur ?
			return null;									// null
		}
	}// get

	@Override
	public void set(int id, Month month, BigDecimal valeur) throws IOException {
		
		// Vérifier que le mois existe dans la multimap
		Map<Month,Map<Integer,BigDecimal>> suivis = cache.getCache();
		if (!suivis.containsKey(month)) {
			suivis.put(month, new HashMap<Integer,BigDecimal>());	// Le créer
		}
		
		// Insérer la valeur
		suivis.get(month).put(id, valeur);
		
		// Signaler le changement
		mustBeSaved = true;
	}// set

	@Override
	public void removeFrom(Month month) throws IOException {
		
		// Parcourir les mois
		Iterator<Month> it = cache.getCache().keySet().iterator();
		while (it.hasNext()) {
			if (!it.next().before(month)) {		// Egal ou postérieur ?
				it.remove();					// Supprimer
				mustBeSaved = true;				// Signaler le changement
			}// if égal ou postérieur
		}// while mois
	}// removeFrom
	
	/** Sauvegarde l'intégralité des montants dans la sous-couche DAO. 
	 * @throws IOException */
	public void save() throws IOException {
		dao.save(cache.getCache());
	}
	
	/** Efface toutes les données du cache. */
	void erase() {
		cache = new CacheSuivi(dao);
	}
}
