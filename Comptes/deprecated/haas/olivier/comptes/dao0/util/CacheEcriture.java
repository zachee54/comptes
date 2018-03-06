package haas.olivier.comptes.dao0.util;

import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.AbstractEcritureDAO;

import java.io.IOException;
import java.util.TreeSet;

/** Encapsule un cache d'Ecriture.
 * Cette classe propose deux types de cache: l'un, trié par ordre naturel à
 * partir d'un AbstractEcritureDAO. L'autre, trié par ordre de pointages à
 * partir d'un autre CacheEcriture.
 * Ainsi les deux instances chargent les données en cascade, seul le premier
 * cache sollicitant les données auprès du DAO sous-jacent. 
 *
 * @author Olivier HAAS
 */
public class CacheEcriture extends AbstractCache<TreeSet<Ecriture>> {
	
	/** DAO sous-jacent. */
	private AbstractEcritureDAO dao = null;
	
	/** Un cache sous-jacent à utiliser comme source pour trier par pointages.
	 */
	private CacheEcriture cacheEcriture = null;
	
	/**
	 * Construit un CacheEcriture pointant vers un AbstractEcritureDAO et
	 * maintenant un cache trié par ordre naturel.
	 * 
	 * @param dao	Le AbstractEcritureDAO contenant les données.
	 */
	public CacheEcriture(AbstractEcritureDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Construit un CacheEcriture pointant vers une autre instance de la même
	 * classe, et maintenant un cache trié par l'ordre des pointages.
	 * 
	 * @param cacheEcriture	Un CacheEcriture contenant les données à utiliser.
	 * @see Ecriture.SortPointages
	 */
	public CacheEcriture(CacheEcriture cacheEcriture) {
		// TODO Pas logique : le sous-dao maintient l'ordre ou le rétablit
		this.cacheEcriture = cacheEcriture;
	}
	
	@Override
	protected TreeSet<Ecriture> load() throws IOException {
		if (dao == null) {		// Pas de DAO : un cache sous-jacent à re-trier
			
			// Créer une collection triée par pointages
			TreeSet<Ecriture> all =
					new TreeSet<Ecriture>(new Ecriture.SortPointages());
			
			// Remplir avec l'autre cache
			all.addAll(cacheEcriture.getCache());
			return all;
			
		} else {				// Un DAO : utiliser les données dans l'ordre
			return dao.getAll();
		}
	}// load
	
	@Override
	// TODO ajouter une gestion des trous
	protected void initMaxId() throws IOException {
		maxId = -1;						// Valeur par défaut
		for (Ecriture e : getCache()) {	// Parcourir le cache
			if (e.id > maxId) {			// Trouvé un plus grand id ?
				maxId = e.id;			// Retenir celui-ci
			}// if id plus grand
		}// for cache
	}// initMaxId

	@Override
	@SuppressWarnings("unchecked")
	public TreeSet<Ecriture> getCloneCache() throws IOException {
		return (TreeSet<Ecriture>) getCache().clone();
	}
}
