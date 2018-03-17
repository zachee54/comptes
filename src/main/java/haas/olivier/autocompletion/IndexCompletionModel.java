package haas.olivier.autocompletion;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** Un modèle de données utilisant un index pour trier les valeurs à suggérer en
 * auto-complétion.
 * Pattern décorateur.
 * 
 * @author Olivier HAAS
 */
public class IndexCompletionModel<T>
implements CompletionModel<T>, Comparator<T> {

	/** Modèle délégué. */
	private CompletionModel<Entry<T,Integer>> delegate;
	
	/** L'index des valeurs. */
	private Map<T,Integer> index = new HashMap<T,Integer>();
	
	/** Le filtre de données. Cette classe utilise son propre filtre,
	 * indépendant du modèle utilisé en sous-couche. */
	private ItemFilter<T> filter;
	
	/** Construit un modèle avec index. 
	 * @param model	Le modèle à utiliser en sous-couche. Il doit implémenter
	 * 				IndexedCompletionModel. Son filtre d'items est ignoré.
	 */
	public IndexCompletionModel(CompletionModel<Entry<T,Integer>> model) {
		delegate = model;						// Le modèle en sous-couche
		setItemFilter(new ItemFilter<T>());		// Un filtre propre
	}// constructeur
	
	/** Reconstruit l'index à partir des données de la sous-couche.
	 * Cette méthode doit être appelée chaque fois que la sous-couche est
	 * susceptible d'avoir modifié ses données.
	 * 
	 * Le temps d'exécution est linéaire.
	 */
	private void refillIndex() {
		
		// Obtenir les valeurs de la sous-couche
		Collection<Entry<T,Integer>> values =	// Récupérer les entrées brutes
				delegate.getValues();
		
		// Rassembler les entrées dans une Map
		index.clear();							// Vider l'index actuel
		for (Entry<T,Integer> e : values) {
			index.put(e.getKey(), e.getValue());// Ajouter cette entrée
		}
	}// refillIndex
	
	@Override
	/** Temps d'exécution linéaire. */
	public void check() {
		delegate.check();						// Vérifier la sous-couche
		refillIndex();							//Reconstruire l'index au cas où
	}// check
	
	@Override
	/** Temps d'exécution linéaire.
	 */
	public void load() {
		delegate.load();						// Vérifier l'actualisation
		refillIndex();							// Reconstruire l'index
	}// load

	@Override
	public Collection<T> getValues() {
		return index.keySet();					// Renvoyer les clés de l'index
	}// getValues
	
	@Override
	public Collection<T> filter(String text) {
		return filter.filter(getValues(), text);
	}
	
	@Override
	public ItemFilter<T> getItemFilter() {
		return filter;
	}
	
	@Override
	public void setItemFilter(ItemFilter<T> filter) {
		this.filter = filter;
		filter.setComparator(this);				// S'imposer comme comparateur
	}

	@Override
	/** Compare deux éléments en fonction de leur nombre d'occurrences dans
	 * l'index.
	 * Si une des données ne figure pas dans l'index, les valeurs sont
	 * rechargées. Si une des valeurs ne figure pas dans les données rechargées,
	 * la méthode lève une exception.
	 * 
	 * Ce comparateur impose un ordre inconsistant avec <code>equals</code>, ce
	 * qui signifie que la comparaison de deux valeurs différentes peut
	 * retourner une valeur 0.
	 * 
	 * Attention : Si la méthode <code>check()</code> n'a pas été appelée
	 * auparavant, ce comparateur peut avoir un comportement imprévisible ou
	 * générer une exception.
	 * 
	 * @throw IllegalArgumentException
	 * 		Si l'une au moins des valeurs ne figure ni dans les données
	 * 		actuelles, ni dans les données rechargées.
	 */
	public int compare(T o1, T o2) {
		return compare(o1, o2, true);
	}
	
	/** Compare deux éléments en fonction de leur nombre d'occurrences dans
	 * l'index.
	 * 
	 * @param secondChance
	 * 			Modifie le comportement de la méthode au cas où l'une des
	 * 			valeurs à comparer ne figure pas dans l'index :
	 * 			- si true, alors la méthode recharge les données et lance un
	 * 			appel récursif avec	secondChance = true.
	 * 			- si false, alors la méthode lève une exception.
	 */
	private int compare(T o1, T o2, boolean secondChance) {	
		
		// Trouver les nombres d'occurrences
		Integer i1 = index.get(o1),
				i2 = index.get(o2);
		
		// Si les données ne figurent pas dans l'index
		if (i1 == null || i2 == null) {
			
			// Si on a rechargé les données
			if (secondChance) {
				
				// Tenter en rechargeant les données
				getValues();							// Recharger
				return compare(o1, o2, false);			// Réessayer
				
			} else {
				
				// Laisser tomber
				throw new IllegalArgumentException("Une de ces données à trier "
						+ "ne figure pas dans les valeurs possibles : "
						+ o1 + ", " + o2);
			}// if secondChance
		}// if i1 ou i2 null

		// Cas général : classer en premier celui qui a le plus d'occurrences
		return -index.get(o1).compareTo(index.get(o2));
	}// compare
}
