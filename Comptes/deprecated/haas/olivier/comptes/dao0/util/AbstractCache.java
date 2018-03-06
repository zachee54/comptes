package haas.olivier.comptes.dao0.util;

import java.io.IOException;

/** Une classe qui encapsule un cache qui se charge à la première utilisation.
 * La classe permet de vérifier systématiquement que le cache est chargé. Elle
 * oblige à choisir entre le renvoi du cache tel quel ou d'un clone.
 *
 * @author Olivier HAAS
 */
public abstract class AbstractCache<T extends Cloneable> {
	
	/** Le cache. */
	protected T cache = null;
	
	/** Le plus grand identifiant utilisé (-1 par défaut). */
	protected int maxId = -1;
	
	/** Un test pour éviter de charger deux fois le cache, et de n'utiliser des
	 * accès synchronisés que lorsque cela paraît nécessaire.
	 */
	private ThreadLocal<Boolean> testFait = new ThreadLocal<Boolean>();
	
	/** Renvoie les données à charger dans le cache. 
	 * @throws IOException */
	protected abstract T load() throws IOException;
	
	/** Recalcule le plus grand identifiant utilisé dans le cache.
	 * @throws IOException 
	 * @see getNextId */
	protected abstract void initMaxId() throws IOException;
	
	/** Renvoie un clone du cache, en vérifiant qu'il est chargé.
	 * @throws IOException
	 */
	public abstract T getCloneCache() throws IOException;
	
	/** Renvoie le cache tel quel, en vérifiant qu'il est chargé. 
	 * @throws IOException
	 */
	public T getCache() throws IOException {
		if (testFait.get() == null) {		// Si le thread n'a pas fait le test
			synchronized(this) {			// Faire le test en synchronisé
				if (cache == null) {		// Pas de cache ?
					cache = load();			// Charger
				}// if cache null
				testFait.set(Boolean.TRUE);	// Marquer le test comme fait
			}// synchronized
		}// if test pas fait
		return cache;
	}// getCache
	
	/** Vide le cache.
	 * La méthode est synchronisée pour mettre à jour les marqueurs en même
	 * temps.
	 */
	public synchronized void clear() {
		cache = null;							// Vide le cache
		maxId = -1;								// Réinitialise le compteur d'id
		testFait = new ThreadLocal<Boolean>();	// Remet à zéro les marqueurs
	}// clear
	
	/** Renvoie le prochain identifiant à utiliser.
	 * En pratique, il s'agit du plus grand identifiant déjà utilisé, augmenté
	 * de 1.
	 * L'appel à cette méthode entraîne le chargement du cache, s'il n'est pas
	 * encore chargé. 
	 * @throws IOException 
	 */
	public int getNextId() throws IOException {
		if (maxId == -1) {						// Si c'est la valeur par défaut
			initMaxId();						// Recalculer maxId
		}
		return ++maxId;
	}// getNextId
}
