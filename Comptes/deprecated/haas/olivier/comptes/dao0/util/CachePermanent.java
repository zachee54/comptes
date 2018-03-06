package haas.olivier.comptes.dao0.util;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.AbstractPermanentDAO;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Encapsule un cache de Permanent à partir des données d'un
 * AbstractPermanentDAO.
 * Les données sont chargées lors de la première utilisation
 *
 * @author Olivier HAAS
 */
public class CachePermanent extends AbstractCache<HashSet<Permanent>> {

	/** Le DAO sous-jacent. */
	private AbstractPermanentDAO dao;
	
	/**
	 * @param dao	Un AbstractPermanentDAO contenant les données à utiliser.
	 */
	public CachePermanent(AbstractPermanentDAO dao) {
		this.dao = dao;
	}
	
	@Override
	protected HashSet<Permanent> load() throws IOException {
		Set<Permanent> all = dao.getAll();		// Récupérer les données
		if (all instanceof HashSet<?>) {		// Bonne classe ?
			return (HashSet<Permanent>) all;	// Retourner tel quel
		} else {
			return new HashSet<Permanent>(all);	// Ou transférer dans un HashSet
		}
	}// load
	
	@Override
	protected void initMaxId() throws IOException {
		maxId = -1;								// Valeur par défaut
		for (Permanent p : getCache()) {		// Parcourir le cache
			if (p.id > maxId) {					// Trouvé un plus grand id ?
				maxId = p.id;					// Retenir celui-ci
			}// if id plus grand
		}// for cache
	}// initMaxId

	@Override
	@SuppressWarnings("unchecked")
	public HashSet<Permanent> getCloneCache() throws IOException {
		return (HashSet<Permanent>) getCache().clone();
	}
}
