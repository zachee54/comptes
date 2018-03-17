package haas.olivier.autocompletion;

import java.util.Collection;

public interface CompletionModel<T> {
	
	/** Renvoie les valeurs contenues dans le modèle.
	 * Le contrat est que cette méthode doit être précédée d'un appel à la
	 * méthode <code>check()</code> pour renvoyer un résultat exact.
	 * 
	 * @return	Une collection des valeurs.
	 */
	public Collection<T> getValues();
	
	/** Renvoie le filtre utilisé. */
	public ItemFilter<T> getItemFilter();
	
	/** Modifie le filtre utilisé. */
	public void setItemFilter(ItemFilter<T> filter);
	
	/** Filtre les valeurs en recherchant le texte spécifié.
	 * 
	 * @param text	Le texte à rechercher.
	 * @return		La collection des valeurs filtrées.
	 */
	public Collection<T> filter(String text);
	
	/** Vérifie que le modèle est à jour et recharge les données si besoin.
	 * Si les données sont rechargées, c'est obligatoirement chargées par la
	 * méthode <code>load()</code>. */
	public void check();
	
	/** Charge les données. */
	public void load();
}
