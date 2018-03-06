package haas.olivier.autocompletion;

import java.util.Collection;

/** Un modèle de données utilisant un cache pour charger les données
 * d'auto-complétion.
 * Pattern decorateur.
 * 
 * @author Olivier HAAS
 */
public class CacheCompletionModel<T> implements CompletionModel<T> {

	/** Interface d'accession au contexte. Cette interface ne propose pas
	 * d'implémentation par défaut, le contexte étant déterminé par le projet.
	 */
	public interface ContextProvider {
		
		/** Renvoie le contexte actuel pour déterminer si le cache doit être
		 * rechargé.
		 */
		public Object getContext();
		
	}// static nested class ContextProvider
	
	/** Modèle délégué. */
	private CompletionModel<T> delegate;
	
	/** Objet d'accession au contexte. */
	private ContextProvider contextProvider;
	
	/** Le contexte connu. L'index n'est rechargé que si le contexte a changé
	 * depuis le dernier chargement. */
	private Object context = null;

	/** Test spécifique à chaque thread avant de lancer une indexation. */
	private ThreadLocal<Object> test = new ThreadLocal<Object>();
	
	/** Construit un modèle avec cache.
	 * 
	 * @param contextProvider	L'objet d'accession au contexte.
	 * @param model				Le modèle à utiliser en sous-couche.
	 */
	public CacheCompletionModel(ContextProvider contextProvider,
			CompletionModel<T> model) {
		this.contextProvider = contextProvider;
		delegate = model;
	}// constructeur
	
	@Override
	/** Vérifie si l'index est à jour.
	 * En pratique, la méthode demande une réindexation chaque fois que le
	 * contexte a changé.
	 * La méthode n'est pas synchronisée, mais la réindexation l'est.
	 * L'algorithme s'assure que la réindexation n'est effectuée qu'une seule
	 * fois pour le même contexte.
	 */
	public void check() {
		Object actual = getContext();		// Le contexte actuel
		if (test.get() != actual) {			// Pas encore vu par ce thread ?
			synchronized (this) {
				if (context != actual) {	// Si le contexte a changé
					context = actual;		// Retenir celui-ci
					load();					// Charger l'index
				}// if contexte changé
			}// synchronized
			test.set(actual);				//Marquer test fait pour ce contexte
		}// if test
	}// checkIndexLoaded

	/** Renvoie le contexte actuel. L'index n'est pas rechargé tant que le
	 * contexte renvoyé par cette méthode n'a pas changé. */
	protected Object getContext() {
		return contextProvider.getContext();
	}
	
	/** Charge les données. Cette méthode n'est appelée qu'en cas de changement
	 * de contexte.
	 */
	public void load() {
		delegate.load();
	}

	@Override
	public Collection<T> getValues() {
		check();							// Vérifier d'abord l'actualisation
		return delegate.getValues();		// Renvoyer les valeurs ensuite
	}
	
	@Override
	public ItemFilter<T> getItemFilter() {
		return delegate.getItemFilter();
	}
	
	@Override
	public void setItemFilter(ItemFilter<T> filter) {
		delegate.setItemFilter(filter);
	}

	@Override
	public Collection<T> filter(String text) {
		return delegate.filter(text);
	}
}
