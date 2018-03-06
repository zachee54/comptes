package haas.olivier.comptes.dao0.buffer;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.comptes.dao.util.CacheSuivi;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Buffer pour les SuiviDAO.
 * 
 * @author Olivier HAAS
 */
public class BufferedSuiviDAO implements SuiviDAO {

	// Sous-couche DAO
	private BufferableSuiviDAO dao;
	
	// Le cache
	private CacheSuivi cache;

	// Mois à partir duquel supprimer les anciennes valeurs
	private Month removeFrom;

	// Montants à redéfinir
	private Map<Month, Map<Integer, BigDecimal>> add =
			new HashMap<Month, Map<Integer, BigDecimal>>();
	
	// Identifiants des comptes à supprimer
	Set<Integer> comptesToRemove = new HashSet<Integer>();
	
	public BufferedSuiviDAO(BufferableSuiviDAO dao) {
		this.dao = dao;						// Mémoriser le DAO
		cache = new CacheSuivi(dao);		// Créer le cache
	}

	/**
	 * Renvoie le montant correspondant au mois et à l'identifiant spécifiés.
	 * 
	 * @param id
	 *            L'identifiant à rechercher dans la sous-map
	 * @param mois
	 *            Le mois à rechercher dans la map
	 * @param map
	 *            La map dans laquelle rechercher
	 * @return Le montant, ou null s'il n'y a pas de montant correspondant aux
	 *         paramètres spécifiés.
	 */
	private BigDecimal getInMultiMap(int id, Month mois,
			Map<Month, Map<Integer, BigDecimal>> map) {

		if (map.containsKey(mois)) {				// Contient le mois
			Map<Integer, BigDecimal> submap = map.get(mois);
			if (submap.containsKey(id)) {			// Contient le compte
				return submap.get(id);				// Retourne le montant
			} else {
				return null;						// Ne contient pas le compte
			}
		} else {
			return null;							// Ne contient pas le mois
		}
	}// getInMultiMap

	@Override
	public BigDecimal get(int id, Month month) {

		// Rechercher dans les montants modifiés dans le buffer
		if (add != null) {
			BigDecimal result = getInMultiMap(id, month, add);

			// S'il est trouvé, le renvoyer
			if (result != null) {
				return result;
			}
		}

		// Ne figure pas dans le buffer. Est-il supprimé ?
		if (removeFrom != null) {
			if (!month.before(removeFrom)) {
				return null; // Supprimé et non redéfini: renvoyer null
			}
		}

		/* Non modifié, non supprimé. Chercher dans le cache et renvoyer le
		 * résultat, qu'il soit null ou pas (on ne trouvera rien de plus dans le
		 * DAO sous-jacent)*/
		try {
			return getInMultiMap(id, month, cache.getCache());
		} catch (IOException e) {
			return null;				// Null si erreur
		}
	}// get

	@Override
	public void set(int id, Month month, BigDecimal valeur) throws IOException {

		// Ajouter ce mois à la multimap des changements s'il n'y figure pas
		if (!add.containsKey(month)) {
			add.put(month, new HashMap<Integer, BigDecimal>());
		}

		// Modifier ou créer la valeur correspondant à ce compte
		add.get(month).put(id, valeur);
	}// set

	@Override
	public void removeFrom(Month debut) throws IOException {

		// Définir la date butoir de suppression
		if (removeFrom == null) {
			// Pas encore de date de suppression: stocker celle-ci
			removeFrom = debut;

		} else if (removeFrom.after(debut)) {
			// La nouvelle date est antérieure à celle qui est stockée: changer
			removeFrom = debut;

		}// Sinon, on a déjà supprimé plus loin que demandé: ne rien faire

		// Supprimer toutes les redéfinitions après cette date
		Iterator<Month> it = add.keySet().iterator();
		while (it.hasNext()) {
			Month month = it.next();
			if (!month.before(debut)) {		// Mois postérieur ou égal à debut
				it.remove();				// Supprimer
			}
		}
	}// removeFrom

	@Override
	public void removeSuiviCompte(int id) throws IOException {
		comptesToRemove.add(id);
	}

	@Override
	public Map<Month, Map<Integer, BigDecimal>> getAll() throws IOException {
		
		// Cloner le cache pour pouvoir modifier son clone
		Map<Month, Map<Integer, BigDecimal>> result = cache.getCloneCache();
		
		// Appliquer les changements intervenus entretemps
		for (Month month : add.keySet()) {
			
			// Créer cette clé si elle n'existe pas
			if (!result.containsKey(month)) {
				result.put(month, new HashMap<Integer, BigDecimal>());
			}
			
			// Définir la sous-map résultat à partir de la sous-map add
			for (Integer id : add.get(month).keySet()) {
				result.get(month).put(id, add.get(month).get(id));
			}
		}
		return result;
	}// getAll

	/** Indique si ce DAO a besoin d'être sauvegardé */
	public boolean mustBeSaved() {

		/*
		 * Dès qu'il y a une date de suppression ou des montants à redéfinir ou
		 * un compte à supprimer du suivi, il faut sauvegarder.
		 */
		return (removeFrom != null) || (!add.isEmpty())
				|| (!comptesToRemove.isEmpty());
	}

	/**
	 * Sauvegarde les données du buffer.
	 * 
	 * @throws IOException
	 */
	void flush() throws IOException {

		// Supprimer les comptes à supprimer dans le suivi
		for (Integer id : comptesToRemove) {
			dao.removeSuiviCompte(id);
		}

		// Sauvegarder
		dao.save(removeFrom, add);

		synchronized(this) {
			
			// Vider tous les éléments du buffer
			add.clear();
			removeFrom = null;
			comptesToRemove.clear();
			
			// Vider le cache
			cache.clear();
		}// synchronized
	}// flush
	
	/** Efface toutes les données du buffer et du cache. */
	void erase() {
		add.clear();
		removeFrom = null;
		cache = new CacheSuivi(dao);
	}

}
