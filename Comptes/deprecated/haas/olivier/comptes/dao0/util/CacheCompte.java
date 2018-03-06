package haas.olivier.comptes.dao0.util;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.AbstractCompteDAO;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Encapsule un cache de Compte pour accéder au AbstractCompteDAO sous-jacent.
 * Les données sont chargées lors de la première utilisation.
 * 
 * @author Olivier HAAS
 */
public class CacheCompte extends AbstractCache<HashSet<Compte>> {

	/** DAO sous-jacent contenant les données d'origine. */
	private AbstractCompteDAO dao;
	
	/** @param dao	Le AbstractCompteDAO contenant les données à utiliser. */
	public CacheCompte(AbstractCompteDAO dao) {
		this.dao = dao;
	}
	
	@Override
	protected HashSet<Compte> load() throws IOException {
		Set<Compte> all = dao.getAll();			// Charger les données
		if (all instanceof HashSet<?>) {		// Déjà de la classe voulue ?
			return (HashSet<Compte>) all;		// Objet utilisable tel quel
		} else {
			return new HashSet<Compte>(all);	// Transférer dans un HashSet
		}
	}// load
	
	@Override
	protected void initMaxId() throws IOException {
		maxId = -1;								// Valeur par défaut
		for (Compte c : getCache()) {			// Parcourir le cache
			if (c.id > maxId) {					// Trouvé un plus grand id ?
				maxId = c.id;					// Retenir celui-là
			}// if id plus grand
		}// for cache
	}// initMaxId

	@Override
	@SuppressWarnings("unchecked")
	public HashSet<Compte> getCloneCache() throws IOException {
		return (HashSet<Compte>) getCache().clone();
	}// getCloneCache
}
