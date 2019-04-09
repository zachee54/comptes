package haas.olivier.autocompletion;

import java.util.ArrayList;
import java.util.Collection;

/** Une implémentation par défaut d'un modèle de données pour les champs de
 * saisie avec auto-complétion.
 * 
 * @author Olivier HAAS
 */
public class DefaultCompletionModel<T> implements CompletionModel<T> {

	/** Les valeurs contenues dans le modèle. */
	private Collection<T> values;
	
	/** Le filtre utilisé dans le modèle. */
	private ItemFilter<T> filter = new ItemFilter<T>();
	
	/** Construit un modèle sans données, avec un filtre par défaut. */
	public DefaultCompletionModel() {
		this(new ArrayList<T>());	// Utiliser une collection vide par défaut
	}// constructeur
	
	/** Construit un modèles contenant les données spécifiées, avec un filtre
	 * par défaut.
	 */
	public DefaultCompletionModel(Collection<T> values) {
		setValues(values);
	}// constructeur
	
	@Override
	public Collection<T> getValues() {
		return values;
	}
	
	/** Remplace les valeurs à suggérer. */
	public void setValues(Collection<T> values) {
		this.values = values;
	}

	@Override
	public ItemFilter<T> getItemFilter() {
		return filter;
	}

	@Override
	public void setItemFilter(ItemFilter<T> filter) {
		this.filter = filter;
	}

	@Override
	public Collection<T> filter(String text) {
		return filter.filter(getValues(), text);
	}

	@Override
	/** Aucune implémentation. */
	public void check() {
	}

	@Override
	/** Aucune implémentation. */
	public void load() {
	}

}
